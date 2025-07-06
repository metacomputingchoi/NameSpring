// model/domain/usecase/ProfileManager.kt
package com.ssc.namespring.model.domain.usecase

import android.content.Context
import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.interfaces.IProfileManager
import com.ssc.namespring.model.domain.service.profile.ProfileManagerImpl

/**
 * ProfileManager - 기존 코드와의 호환성을 위한 Facade
 * 내부적으로는 의존성 주입된 ProfileManagerImpl을 사용
 */
object ProfileManager {
    private const val TAG = "ProfileManager"

    private lateinit var implementation: IProfileManager
    private var isInitialized = false

    // 기존 SortType을 유지하면서 인터페이스의 SortType으로 위임
    enum class SortType {
        NAME_ASC, NAME_DESC, SCORE_DESC, SCORE_ASC, DATE_DESC, DATE_ASC
    }

    fun init(context: Context) {
        if (isInitialized) {
            Log.d(TAG, "ProfileManager already initialized")
            return
        }

        // ProfileDependencyContainer는 internal이므로 직접 생성
        val container = ProfileDependencyContainer(context)
        implementation = ProfileManagerImpl(container)
        implementation.init()

        isInitialized = true
        Log.d(TAG, "ProfileManager initialized with implementation")
    }

    fun setImplementation(impl: IProfileManager) {
        implementation = impl
        isInitialized = true
        Log.d(TAG, "ProfileManager implementation injected")
    }

    fun addProfile(profile: Profile): Boolean {
        ensureInitialized()
        return implementation.addProfile(profile)
    }

    fun updateProfile(profile: Profile): Boolean {
        ensureInitialized()
        return implementation.updateProfile(profile)
    }

    fun deleteProfiles(profileIds: List<String>) {
        ensureInitialized()
        implementation.deleteProfiles(profileIds)
    }

    fun deleteProfile(profileId: String) {
        deleteProfiles(listOf(profileId))
    }

    fun isDuplicateProfile(profile: Profile): Boolean {
        ensureInitialized()
        return implementation.isDuplicateProfile(profile)
    }

    fun searchProfiles(query: String): List<Profile> {
        ensureInitialized()
        return implementation.searchProfiles(query)
    }

    fun getSortedProfiles(sortType: SortType): List<Profile> {
        ensureInitialized()
        // SortType 매핑
        val implSortType = when (sortType) {
            SortType.NAME_ASC -> IProfileManager.SortType.NAME_ASC
            SortType.NAME_DESC -> IProfileManager.SortType.NAME_DESC
            SortType.SCORE_DESC -> IProfileManager.SortType.SCORE_DESC
            SortType.SCORE_ASC -> IProfileManager.SortType.SCORE_ASC
            SortType.DATE_DESC -> IProfileManager.SortType.DATE_DESC
            SortType.DATE_ASC -> IProfileManager.SortType.DATE_ASC
        }
        return implementation.getSortedProfiles(implSortType)
    }

    fun getAllProfiles(): List<Profile> {
        ensureInitialized()
        return implementation.getAllProfiles()
    }

    fun getProfile(id: String): Profile? {
        ensureInitialized()
        return implementation.getProfile(id)
    }

    fun getCurrentProfile(): Profile? {
        ensureInitialized()
        return implementation.getCurrentProfile()
    }

    fun hasProfiles(): Boolean {
        ensureInitialized()
        return implementation.hasProfiles()
    }

    fun setSelectedProfile(profile: Profile) {
        ensureInitialized()
        implementation.setSelectedProfile(profile)
    }

    fun getSelectedProfile(): Profile? {
        ensureInitialized()
        return implementation.getSelectedProfile()
    }

    fun switchProfile(id: String) {
        ensureInitialized()
        implementation.switchProfile(id)
    }

    private fun ensureInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("ProfileManager must be initialized before use")
        }
    }
}