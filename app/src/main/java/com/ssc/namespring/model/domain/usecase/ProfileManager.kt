// model/domain/usecase/ProfileManager.kt
package com.ssc.namespring.model.domain.usecase

import android.content.Context
import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile

/**
 * ProfileManager - Facade pattern을 사용하여 기존 인터페이스 유지
 * 내부적으로는 책임이 분리된 클래스들에게 위임
 */
object ProfileManager {
    private const val TAG = "ProfileManager"

    private lateinit var container: ProfileDependencyContainer
    private lateinit var useCase: ProfileUseCase

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) {
            Log.d(TAG, "ProfileManager already initialized")
            return
        }

        container = ProfileDependencyContainer(context)
        useCase = container.provideProfileUseCase()

        useCase.initialize()
        isInitialized = true

        Log.d(TAG, "ProfileManager initialized successfully")
    }

    fun addProfile(profile: Profile): Boolean {
        ensureInitialized()
        return useCase.addProfile(profile)
    }

    fun updateProfile(profile: Profile): Boolean {
        ensureInitialized()
        return useCase.updateProfile(profile)
    }

    fun deleteProfiles(profileIds: List<String>) {
        ensureInitialized()
        useCase.deleteProfiles(profileIds)
    }

    fun deleteProfile(profileId: String) {
        deleteProfiles(listOf(profileId))
    }

    fun isDuplicateProfile(profile: Profile): Boolean {
        ensureInitialized()
        return useCase.isDuplicateProfile(profile)
    }

    fun searchProfiles(query: String): List<Profile> {
        ensureInitialized()
        return useCase.searchProfiles(query)
    }

    fun getSortedProfiles(sortType: SortType): List<Profile> {
        ensureInitialized()
        return useCase.getSortedProfiles(sortType)
    }

    fun getAllProfiles(): List<Profile> {
        ensureInitialized()
        return useCase.getAllProfiles()
    }

    fun getProfile(id: String): Profile? {
        ensureInitialized()
        return useCase.getProfile(id)
    }

    fun getCurrentProfile(): Profile? {
        ensureInitialized()
        return useCase.getCurrentProfile()
    }

    fun hasProfiles(): Boolean {
        ensureInitialized()
        return useCase.hasProfiles()
    }

    fun setSelectedProfile(profile: Profile) {
        ensureInitialized()
        useCase.setSelectedProfile(profile)
    }

    fun getSelectedProfile(): Profile? {
        ensureInitialized()
        return useCase.getSelectedProfile()
    }

    fun switchProfile(id: String) {
        ensureInitialized()
        useCase.switchProfile(id)
    }

    private fun ensureInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("ProfileManager must be initialized before use")
        }
    }

    enum class SortType {
        NAME_ASC, NAME_DESC, SCORE_DESC, SCORE_ASC, DATE_DESC, DATE_ASC
    }
}