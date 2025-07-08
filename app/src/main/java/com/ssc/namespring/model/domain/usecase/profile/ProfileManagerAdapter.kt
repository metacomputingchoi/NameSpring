// model/domain/usecase/profile/ProfileManagerAdapter.kt
package com.ssc.namespring.model.domain.usecase.profile

import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.interfaces.IProfileManager
import com.ssc.namespring.model.domain.usecase.ProfileManager

/**
 * IProfileManager를 ProfileManager 인터페이스로 변환하는 어댑터
 */
internal class ProfileManagerAdapter(
    private val implementation: IProfileManager
) : ProfileManager {

    override fun addProfile(profile: Profile): Boolean =
        implementation.addProfile(profile)

    override fun updateProfile(profile: Profile): Boolean =
        implementation.updateProfile(profile)

    override fun deleteProfiles(profileIds: List<String>) =
        implementation.deleteProfiles(profileIds)

    override fun deleteProfile(profileId: String) =
        deleteProfiles(listOf(profileId))

    override fun isDuplicateProfile(profile: Profile): Boolean =
        implementation.isDuplicateProfile(profile)

    override fun searchProfiles(query: String): List<Profile> =
        implementation.searchProfiles(query)

    override fun getSortedProfiles(sortType: ProfileManager.SortType): List<Profile> {
        val implSortType = when (sortType) {
            ProfileManager.SortType.NAME_ASC -> IProfileManager.SortType.NAME_ASC
            ProfileManager.SortType.NAME_DESC -> IProfileManager.SortType.NAME_DESC
            ProfileManager.SortType.SCORE_DESC -> IProfileManager.SortType.SCORE_DESC
            ProfileManager.SortType.SCORE_ASC -> IProfileManager.SortType.SCORE_ASC
            ProfileManager.SortType.DATE_DESC -> IProfileManager.SortType.DATE_DESC
            ProfileManager.SortType.DATE_ASC -> IProfileManager.SortType.DATE_ASC
        }
        return implementation.getSortedProfiles(implSortType)
    }

    override fun getAllProfiles(): List<Profile> =
        implementation.getAllProfiles()

    override fun getProfile(id: String): Profile? =
        implementation.getProfile(id)

    override fun getCurrentProfile(): Profile? =
        implementation.getCurrentProfile()

    override fun hasProfiles(): Boolean =
        implementation.hasProfiles()

    override fun setSelectedProfile(profile: Profile) =
        implementation.setSelectedProfile(profile)

    override fun getSelectedProfile(): Profile? =
        implementation.getSelectedProfile()

    override fun switchProfile(id: String) =
        implementation.switchProfile(id)
}