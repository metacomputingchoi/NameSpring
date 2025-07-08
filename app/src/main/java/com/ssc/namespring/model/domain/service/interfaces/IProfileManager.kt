// model/domain/service/interfaces/IProfileManager.kt
package com.ssc.namespring.model.domain.service.interfaces

import com.ssc.namespring.model.domain.entity.Profile

interface IProfileManager {
    fun init()
    fun addProfile(profile: Profile): Boolean
    fun updateProfile(profile: Profile): Boolean
    fun deleteProfiles(profileIds: List<String>)
    fun deleteProfile(profileId: String)
    fun isDuplicateProfile(profile: Profile): Boolean
    fun searchProfiles(query: String): List<Profile>
    fun getSortedProfiles(profileSortType: ProfileSortType): List<Profile>
    fun getAllProfiles(): List<Profile>
    fun getProfile(id: String): Profile?
    fun getCurrentProfile(): Profile?
    fun hasProfiles(): Boolean
    fun setSelectedProfile(profile: Profile)
    fun getSelectedProfile(): Profile?
    fun switchProfile(id: String)

    enum class ProfileSortType {
        NAME_ASC, NAME_DESC, SCORE_DESC, SCORE_ASC, DATE_DESC, DATE_ASC
    }
}