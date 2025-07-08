// model/domain/usecase/profile/ProfileSearchHandler.kt
package com.ssc.namespring.model.domain.usecase.profile

import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.interfaces.IProfileService
import com.ssc.namespring.model.domain.service.interfaces.IProfileManager

/**
 * 프로필 검색 및 정렬을 담당하는 클래스
 */
internal class ProfileSearchHandler(
    private val service: IProfileService
) {
    fun searchProfiles(query: String): List<Profile> = 
        service.searchProfiles(query)

    fun getSortedProfiles(profileSortType: IProfileManager.ProfileSortType): List<Profile> =
        service.getSortedProfiles(profileSortType)

    fun isDuplicateProfile(profile: Profile): Boolean =
        service.getAllProfiles().any { it.equals(profile) && it.id != profile.id }

    fun hasProfiles(): Boolean = service.hasProfiles()
}