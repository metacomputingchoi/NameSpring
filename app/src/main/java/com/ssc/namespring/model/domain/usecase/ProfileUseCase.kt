// model/domain/usecase/ProfileUseCase.kt
package com.ssc.namespring.model.domain.usecase

import android.util.Log
import com.google.gson.JsonSyntaxException
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.interfaces.IProfileManager
import com.ssc.namespring.model.domain.service.interfaces.IProfileRepository
import com.ssc.namespring.model.domain.service.interfaces.IProfileService
import com.ssc.namespring.model.domain.service.interfaces.IProfileEvaluator
import com.ssc.namespring.model.domain.service.interfaces.IProfileMigrator

/**
 * 프로필 관련 비즈니스 로직을 담당하는 UseCase
 */
class ProfileUseCase(
    private val repository: IProfileRepository,
    private val service: IProfileService,
    private val evaluator: IProfileEvaluator,
    private val migrator: IProfileMigrator
) {
    companion object {
        private const val TAG = "ProfileUseCase"
    }

    fun initialize() {
        loadProfiles()
        updateProfilesIfNeeded()
    }

    fun addProfile(profile: Profile): Boolean {
        Log.d(TAG, "ProfileUseCase.addProfile 시작")
        Log.d(TAG, "  - 평가 전 evaluatedNameJson: ${profile.evaluatedNameJson?.length}")

        val evaluatedProfile = evaluator.evaluate(profile)

        Log.d(TAG, "  - 평가 후 evaluatedNameJson: ${evaluatedProfile.evaluatedNameJson?.length}")
        Log.d(TAG, "  - 평가 후 nameBomScore: ${evaluatedProfile.nameBomScore}")

        val success = service.addProfile(evaluatedProfile)
        if (success) saveProfiles()
        return success
    }

    fun updateProfile(profile: Profile): Boolean {
        Log.d(TAG, "ProfileUseCase.updateProfile 시작")
        Log.d(TAG, "  - 평가 전 evaluatedNameJson: ${profile.evaluatedNameJson?.length}")

        val evaluatedProfile = evaluator.evaluate(profile)

        Log.d(TAG, "  - 평가 후 evaluatedNameJson: ${evaluatedProfile.evaluatedNameJson?.length}")
        Log.d(TAG, "  - 평가 후 nameBomScore: ${evaluatedProfile.nameBomScore}")

        val success = service.updateProfile(evaluatedProfile)
        if (success) saveProfiles()
        return success
    }

    fun deleteProfiles(profileIds: List<String>) {
        service.deleteProfiles(profileIds)
        saveProfiles()
    }

    fun isDuplicateProfile(profile: Profile): Boolean =
        service.getAllProfiles().any { it.equals(profile) && it.id != profile.id }

    fun searchProfiles(query: String): List<Profile> =
        service.searchProfiles(query)

    fun getSortedProfiles(sortType: IProfileManager.SortType): List<Profile> =
        service.getSortedProfiles(sortType)

    fun getAllProfiles(): List<Profile> =
        service.getAllProfiles()

    fun getProfile(id: String): Profile? =
        service.getProfile(id)

    fun getCurrentProfile(): Profile? =
        service.getCurrentProfile()

    fun hasProfiles(): Boolean =
        service.hasProfiles()

    fun setSelectedProfile(profile: Profile) =
        service.setSelectedProfile(profile)

    fun getSelectedProfile(): Profile? =
        service.getSelectedProfile()

    fun switchProfile(id: String) {
        if (service.switchProfile(id)) {
            saveProfiles()
        }
    }

    private fun loadProfiles() {
        val json = repository.loadProfilesJson()
        if (json != null) {
            try {
                val profiles = repository.loadProfiles()
                val currentId = repository.loadCurrentProfileId()
                service.initProfiles(profiles, currentId)
            } catch (e: JsonSyntaxException) {
                Log.w(TAG, "Legacy profile format detected", e)
                handleLegacyProfiles(json)
            }
        } else {
            service.initProfiles(emptyList(), null)
        }
    }

    private fun handleLegacyProfiles(json: String) {
        val migratedProfiles = migrator.migrateFromJson(json)
        if (migratedProfiles != null) {
            service.initProfiles(migratedProfiles, null)
            saveProfiles()
            Log.i(TAG, "Successfully migrated ${migratedProfiles.size} legacy profiles")
        } else {
            repository.clearProfiles()
            service.initProfiles(emptyList(), null)
            Log.w(TAG, "Failed to migrate legacy profiles, starting fresh")
        }
    }

    private fun updateProfilesIfNeeded() {
        val profiles = service.getAllProfiles()

        // 완전한 정보를 가진 프로필만 평가
        val profilesToEvaluate = profiles.filter { profile ->
            // 평가되지 않았고 완전한 정보를 가진 경우만
            profile.nameBomScore == 0 &&
                    hasCompleteInfo(profile)
        }

        Log.d(TAG, "평가 필요한 프로필: ${profilesToEvaluate.size}개")

        var hasChanges = false
        profilesToEvaluate.forEach { profile ->
            val evaluatedProfile = evaluator.evaluate(profile)
            service.replaceProfile(profile, evaluatedProfile)
            hasChanges = true
        }

        if (hasChanges) {
            saveProfiles()
            Log.d(TAG, "프로필 평가 완료 및 저장")
        }
    }

    private fun hasCompleteInfo(profile: Profile): Boolean {
        val hasCompleteName = profile.givenName?.let { givenName ->
            givenName.charInfos.isNotEmpty() &&
                    givenName.charInfos.all {
                        it.korean.isNotEmpty() && it.hanja.isNotEmpty()
                    }
        } == true

        return profile.surname != null && hasCompleteName
    }

    private fun saveProfiles() {
        repository.saveProfiles(service.getAllProfiles(), service.getCurrentProfileId())
    }
}