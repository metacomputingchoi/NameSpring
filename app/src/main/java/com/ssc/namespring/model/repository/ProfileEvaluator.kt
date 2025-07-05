// model/repository/ProfileEvaluator.kt
package com.ssc.namespring.model.repository

import android.util.Log
import com.ssc.namingengine.NamingEngine
import com.ssc.namespring.model.data.GivenNameInfo
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.data.SurnameInfo
import com.ssc.namespring.model.utils.SajuEvaluator
import java.time.ZoneId

internal class ProfileEvaluator(private val namingEngine: NamingEngine) {
    companion object {
        private const val TAG = "ProfileEvaluator"
    }

    fun evaluateProfile(profile: Profile): Profile {

        val hasCompleteName = profile.givenName?.let { givenName ->
            givenName.charInfos.isNotEmpty() &&
                    givenName.charInfos.all { it.korean.isNotEmpty() && it.hanja.isNotEmpty() }
        } ?: false

        return if (hasCompleteName && profile.surname != null) {
            evaluateFullProfile(profile)
        } else {
            SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
        }
    }

    fun updateProfilesIfNeeded(profiles: List<Profile>): List<Profile> {
        if (namingEngine == null) return profiles

        val profilesToUpdate = profiles.filter { it.ohaengInfo == null }
        Log.d(TAG, "재평가 필요한 프로필: ${profilesToUpdate.size}개")

        return profiles.map { profile ->
            if (profile.ohaengInfo == null) {
                Log.d(TAG, "프로필 재평가 시작: ${profile.profileName}")
                val evaluated = SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
                Log.d(TAG, "프로필 재평가 완료: ${profile.profileName}, 오행: ${evaluated.ohaengInfo}")
                evaluated
            } else {
                profile
            }
        }
    }

    private fun evaluateFullProfile(profile: Profile): Profile {
        val givenName = profile.givenName ?: return profile
        val surname = profile.surname ?: return profile

        val nameInput = buildNameInput(surname, givenName)
        Log.d(TAG, "전체 평가 시작 - 이름: $nameInput")

        return try {
            val birthDateTime = profile.birthDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            val evaluatedNames = namingEngine!!.generateNames(
                userInput = nameInput,
                birthDateTime = birthDateTime,
                useYajasi = profile.isYajaTime,
                verbose = true,
                withoutFilter = true
            )

            evaluatedNames.firstOrNull()?.let { generatedName ->
                profile.apply { updateFromGeneratedName(generatedName) }
            } ?: SajuEvaluator.evaluateProfileSaju(profile, namingEngine)

        } catch (e: Exception) {
            Log.e(TAG, "전체 평가 실패 - ID: ${profile.id}", e)
            SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
        }
    }

    private fun buildNameInput(surname: SurnameInfo, givenName: GivenNameInfo): String {
        val surnameInput = "[${surname.korean}/${surname.hanja}]"
        val givenNameInput = givenName.charInfos.joinToString("") { charInfo ->
            "[${charInfo.korean}/${charInfo.hanja}]"
        }
        return surnameInput + givenNameInput
    }
}