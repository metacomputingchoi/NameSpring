// model/domain/service/profile/helpers/ProfileUpdateHelper.kt
package com.ssc.namespring.model.domain.service.profile.helpers

import android.util.Log
import com.google.gson.Gson
import com.ssc.namespring.model.domain.entity.*
import com.ssc.namingengine.data.GeneratedName

internal class ProfileUpdateHelper {
    companion object {
        private const val TAG = "ProfileUpdateHelper"
        private val gson = Gson()
    }

    fun updateProfileWithGeneratedName(
        profile: Profile,
        givenName: GivenNameInfo,
        generatedName: GeneratedName
    ): Profile {
        val jsonTest = gson.toJson(generatedName)
        Log.d(TAG, "JSON 변환 성공: ${jsonTest.length} bytes")

        profile.givenName = updateGivenNameInfoFromGeneratedName(givenName, generatedName)
        profile.evaluatedNameJson = jsonTest

        Log.d(TAG, "=== 평가 결과 ===")
        Log.d(TAG, "  - nameBomScore: ${profile.nameBomScore}")
        Log.d(TAG, "  - evaluatedNameJson: ${profile.evaluatedNameJson?.length} bytes")

        return profile
    }

    private fun updateGivenNameInfoFromGeneratedName(
        givenNameInfo: GivenNameInfo,
        generatedName: GeneratedName
    ): GivenNameInfo {
        val updatedCharInfos = givenNameInfo.charInfos.mapIndexed { index, existingCharInfo ->
            val hanjaDetail = generatedName.hanjaDetails.getOrNull(index + 1)

            CharInfo(
                korean = existingCharInfo.korean,
                hanja = existingCharInfo.hanja,
                meaning = hanjaDetail?.inmyongMeaning,
                strokes = hanjaDetail?.okpyeonHoeksu 
                    ?: generatedName.nameHanjaHoeksu.getOrNull(index) ?: 0,
                ohaeng = hanjaDetail?.jawonOhaeng,
                eumyang = hanjaDetail?.baleumEumyang?.toIntOrNull() ?: 0
            )
        }

        return givenNameInfo.copy(charInfos = updatedCharInfos)
    }

    fun shouldUpdateProfile(profile: Profile): Boolean {
        return profile.ohaengInfo == null ||
                (profile.isEvaluated() && profile.evaluatedNameJson == null)
    }
}