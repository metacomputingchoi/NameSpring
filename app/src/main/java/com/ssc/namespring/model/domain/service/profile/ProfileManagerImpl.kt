// model/domain/service/profile/ProfileManagerImpl.kt
package com.ssc.namespring.model.domain.service.profile

import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.interfaces.IProfileManager
import com.ssc.namespring.model.domain.service.interfaces.IProfileDependencyProvider
import com.ssc.namespring.model.domain.usecase.ProfileUseCase

class ProfileManagerImpl(
    private val dependencyProvider: IProfileDependencyProvider
) : IProfileManager {

    companion object {
        private const val TAG = "ProfileManagerImpl"
    }

    private lateinit var useCase: ProfileUseCase
    private var isInitialized = false

    override fun init() {
        if (isInitialized) {
            Log.d(TAG, "ProfileManager already initialized")
            return
        }

        useCase = dependencyProvider.provideProfileUseCase()
        useCase.initialize()
        isInitialized = true

        Log.d(TAG, "ProfileManager initialized successfully")
    }

    override fun addProfile(profile: Profile): Boolean {
        ensureInitialized()

        Log.d(TAG, "addProfile 호출:")
        Log.d(TAG, "  - evaluatedName: ${profile.evaluatedName != null}")
        Log.d(TAG, "  - evaluatedNameJson 길이: ${profile.evaluatedNameJson?.length}")
        Log.d(TAG, "  - nameBomScore: ${profile.nameBomScore}")

        return useCase.addProfile(profile)
    }

    override fun updateProfile(profile: Profile): Boolean {
        ensureInitialized()

        Log.d(TAG, "updateProfile 호출:")
        Log.d(TAG, "  - evaluatedName: ${profile.evaluatedName != null}")
        Log.d(TAG, "  - evaluatedNameJson 길이: ${profile.evaluatedNameJson?.length}")
        Log.d(TAG, "  - nameBomScore: ${profile.nameBomScore}")

        return useCase.updateProfile(profile)
    }

    override fun deleteProfiles(profileIds: List<String>) {
        ensureInitialized()
        useCase.deleteProfiles(profileIds)
    }

    override fun deleteProfile(profileId: String) {
        deleteProfiles(listOf(profileId))
    }

    override fun isDuplicateProfile(profile: Profile): Boolean {
        ensureInitialized()
        return useCase.isDuplicateProfile(profile)
    }

    override fun searchProfiles(query: String): List<Profile> {
        ensureInitialized()
        return useCase.searchProfiles(query)
    }

    override fun getSortedProfiles(profileSortType: IProfileManager.ProfileSortType): List<Profile> {
        ensureInitialized()
        return useCase.getSortedProfiles(profileSortType)
    }

    override fun getAllProfiles(): List<Profile> {
        ensureInitialized()
        return useCase.getAllProfiles()
    }

    override fun getProfile(id: String): Profile? {
        ensureInitialized()
        return useCase.getProfile(id)
    }

    override fun getCurrentProfile(): Profile? {
        ensureInitialized()
        return useCase.getCurrentProfile()
    }

    override fun hasProfiles(): Boolean {
        ensureInitialized()
        return useCase.hasProfiles()
    }

    override fun setSelectedProfile(profile: Profile) {
        ensureInitialized()
        useCase.setSelectedProfile(profile)
    }

    override fun getSelectedProfile(): Profile? {
        ensureInitialized()
        return useCase.getSelectedProfile()
    }

    override fun switchProfile(id: String) {
        ensureInitialized()
        useCase.switchProfile(id)
    }

    private fun ensureInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("ProfileManager must be initialized before use")
        }
    }
}