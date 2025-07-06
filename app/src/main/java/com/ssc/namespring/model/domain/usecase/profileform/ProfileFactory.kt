// model/domain/usecase/profileform/ProfileFactory.kt
package com.ssc.namespring.model.domain.usecase.profileform

import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import java.util.Calendar

class ProfileFactory {

    fun createProfile(
        profileId: String?,
        profileName: String,
        birthDate: Calendar,
        isYajaTime: Boolean,
        surname: SurnameInfo?,
        givenName: GivenNameInfo?,
        existingProfile: Profile? = null
    ): Profile {
        return if (!profileId.isNullOrEmpty() && existingProfile != null) {
            // 기존 프로필 업데이트
            Profile(
                id = profileId,
                profileName = profileName,
                birthDate = birthDate,
                isYajaTime = isYajaTime,
                surname = surname,
                givenName = givenName,
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