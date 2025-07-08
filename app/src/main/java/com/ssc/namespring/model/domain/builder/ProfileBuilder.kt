// model/domain/builder/ProfileBuilder.kt
package com.ssc.namespring.model.domain.builder

import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFactory
import java.util.Calendar

/**
 * Profile 생성을 담당하는 빌더
 */
internal class ProfileBuilder(
    private val profileFactory: ProfileFactory = ProfileFactory(),
    private val logger: ProfileFormLogger = ProfileFormLogger()
) {

    fun build(
        profileId: String?,
        profileName: String,
        birthDate: Calendar,
        isYajaTime: Boolean,
        surname: SurnameInfo?,
        givenNameInfo: GivenNameInfo?,
        existingProfile: Profile?
    ): Profile {

        logger.logProfileCreation(profileName, givenNameInfo)

        val profile = profileFactory.createProfile(
            profileId = profileId,
            profileName = profileName,
            birthDate = birthDate,
            isYajaTime = isYajaTime,
            surname = surname,
            givenName = givenNameInfo,
            existingProfile = existingProfile
        )

        logger.logCreatedProfile(profile)

        return profile
    }
}