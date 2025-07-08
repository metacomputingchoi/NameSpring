// model/domain/service/profile/ProfileServiceImpl.kt
package com.ssc.namespring.model.domain.service.profile

import android.util.Log
import com.ssc.namespring.model.domain.service.interfaces.IProfileManager
import com.ssc.namespring.model.domain.service.interfaces.ProfileService
import com.ssc.namespring.model.domain.entity.Profile

class ProfileServiceImpl : ProfileService {
    companion object {
        private const val TAG = "ProfileServiceImpl"
    }

    private val profiles = mutableListOf<Profile>()
    private var currentProfileId: String? = null
    private var selectedProfile: Profile? = null
    private val searchService = ProfileSearchService()

    fun initProfiles(loadedProfiles: List<Profile>, loadedCurrentId: String?) {
        profiles.clear()
        profiles.addAll(loadedProfiles)
        currentProfileId = loadedCurrentId
    }

    override fun addProfile(profile: Profile): Boolean {
        if (isDuplicate(profile)) {
            Log.w(TAG, "중복된 프로필 추가 시도")
            return false
        }

        profiles.add(profile)
        if (profiles.size == 1) {
            currentProfileId = profile.id
        }
        Log.d(TAG, "프로필 추가 완료 - ID: ${profile.id}")
        return true
    }

    override fun updateProfile(profile: Profile): Boolean {
        if (isDuplicate(profile)) return false

        val index = profiles.indexOfFirst { it.id == profile.id }
        if (index != -1) {
            profiles[index] = profile.copy(updatedAt = System.currentTimeMillis())
            Log.d(TAG, "프로필 업데이트 완료 - ID: ${profile.id}")
            return true
        }
        return false
    }

    override fun deleteProfiles(profileIds: List<String>) {
        profiles.removeIf { it.id in profileIds }
        if (profileIds.contains(currentProfileId)) {
            currentProfileId = profiles.firstOrNull()?.id
        }
        Log.d(TAG, "${profileIds.size}개 프로필 삭제 완료")
    }

    override fun getProfile(id: String): Profile? = profiles.find { it.id == id }

    override fun getAllProfiles(): List<Profile> = profiles.toList()

    override fun getCurrentProfile(): Profile? = currentProfileId?.let { getProfile(it) }

    override fun switchProfile(id: String): Boolean {
        if (profiles.any { it.id == id }) {
            currentProfileId = id
            return true
        }
        return false
    }

    fun getCurrentProfileId(): String? = currentProfileId
    fun hasProfiles(): Boolean = profiles.isNotEmpty()
    fun setSelectedProfile(profile: Profile) { selectedProfile = profile }
    fun getSelectedProfile(): Profile? = selectedProfile

    fun searchProfiles(query: String): List<Profile> {
        if (query.isEmpty()) return getAllProfiles()
        return searchService.search(profiles, query)
    }

    fun getSortedProfiles(profileSortType: IProfileManager.ProfileSortType): List<Profile> {
        return when (profileSortType) {
            IProfileManager.ProfileSortType.NAME_ASC -> profiles.sortedBy { it.profileName }
            IProfileManager.ProfileSortType.NAME_DESC -> profiles.sortedByDescending { it.profileName }
            IProfileManager.ProfileSortType.SCORE_DESC -> profiles.sortedByDescending { it.nameBomScore }
            IProfileManager.ProfileSortType.SCORE_ASC -> profiles.sortedBy { it.nameBomScore }
            IProfileManager.ProfileSortType.DATE_DESC -> profiles.sortedByDescending { it.createdAt }
            IProfileManager.ProfileSortType.DATE_ASC -> profiles.sortedBy { it.createdAt }
        }
    }

    fun replaceProfile(oldProfile: Profile, newProfile: Profile) {
        val index = profiles.indexOfFirst { it.id == oldProfile.id }
        if (index != -1) {
            profiles[index] = newProfile
        }
    }

    private fun isDuplicate(profile: Profile): Boolean {
        return profiles.any { it.equals(profile) && it.id != profile.id }
    }
}