// model/domain/service/interfaces/IProfileService.kt
package com.ssc.namespring.model.domain.service.interfaces

import com.ssc.namespring.model.domain.entity.Profile

interface IProfileService {
    fun initProfiles(profiles: List<Profile>, currentProfileId: String?)
    fun addProfile(profile: Profile): Boolean
    fun updateProfile(profile: Profile): Boolean
    fun deleteProfiles(profileIds: List<String>)
    fun getProfile(id: String): Profile?
    fun getAllProfiles(): List<Profile>
    fun getCurrentProfile(): Profile?
    fun switchProfile(id: String): Boolean
    fun searchProfiles(query: String): List<Profile>
    fun getSortedProfiles(profileSortType: IProfileManager.ProfileSortType): List<Profile>
    fun hasProfiles(): Boolean
    fun setSelectedProfile(profile: Profile)
    fun getSelectedProfile(): Profile?
    fun getCurrentProfileId(): String?
    fun replaceProfile(oldProfile: Profile, newProfile: Profile)
}