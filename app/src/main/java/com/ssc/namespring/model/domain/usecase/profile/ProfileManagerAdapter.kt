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

    override fun getSortedProfiles(profileManagerSortType: ProfileManager.ProfileManagerSortType): List<Profile> {
        val implProfileProfileManagerSortType = when (profileManagerSortType) {
            ProfileManager.ProfileManagerSortType.NAME_ASC -> IProfileManager.ProfileSortType.NAME_ASC
            ProfileManager.ProfileManagerSortType.NAME_DESC -> IProfileManager.ProfileSortType.NAME_DESC
            ProfileManager.ProfileManagerSortType.SCORE_DESC -> IProfileManager.ProfileSortType.SCORE_DESC
            ProfileManager.ProfileManagerSortType.SCORE_ASC -> IProfileManager.ProfileSortType.SCORE_ASC
            ProfileManager.ProfileManagerSortType.DATE_DESC -> IProfileManager.ProfileSortType.DATE_DESC
            ProfileManager.ProfileManagerSortType.DATE_ASC -> IProfileManager.ProfileSortType.DATE_ASC
        }
        return implementation.getSortedProfiles(implProfileProfileManagerSortType)
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