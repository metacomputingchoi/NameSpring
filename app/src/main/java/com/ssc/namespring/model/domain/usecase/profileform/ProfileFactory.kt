// model/domain/usecase/profileform/ProfileFactory.kt
package com.ssc.namespring.model.domain.usecase.profileform

import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import java.util.Calendar

class ProfileFactory {
    companion object {
        private const val TAG = "ProfileFactory"
    }

    fun createProfile(
        profileId: String?,
        profileName: String,
        birthDate: Calendar,
        isYajaTime: Boolean,
        surname: SurnameInfo?,
        givenName: GivenNameInfo?,
        existingProfile: Profile? = null
    ): Profile {
        Log.d(TAG, "createProfile: profileName='$profileName', surname=${surname?.korean}(${surname?.hanja})")

        if (givenName != null) {
            Log.d(TAG, "GivenName: korean='${givenName.korean}', hanja='${givenName.hanja}'")
            givenName.charInfos.forEachIndexed { index, charInfo ->
                Log.d(TAG, "  CharInfo[$index]: korean='${charInfo.korean}', hanja='${charInfo.hanja}'")
            }
        }

        return if (!profileId.isNullOrEmpty() && existingProfile != null) {
            // 기존 프로필 업데이트 - 평가 정보 유지
            Profile(
                id = profileId,
                profileName = profileName,
                birthDate = birthDate,
                isYajaTime = isYajaTime,
                surname = surname,
                givenName = givenName,
                nameBomScore = 0,  // 재평가를 위해 초기화
                sajuInfo = null,   // 재평가를 위해 초기화
                ohaengInfo = null, // 재평가를 위해 초기화
                evaluatedNameJson = null, // 재평가를 위해 초기화
                createdAt = existingProfile.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            // 새 프로필 생성
            Profile(
                profileName = profileName,
                birthDate = birthDate,
                isYajaTime = isYajaTime,
                surname = surname,
                givenName = givenName
            )
        }
    }
}