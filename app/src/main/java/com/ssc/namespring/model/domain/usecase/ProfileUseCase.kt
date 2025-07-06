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
        val evaluatedProfile = evaluator.evaluate(profile)
        val success = service.addProfile(evaluatedProfile)
        if (success) saveProfiles()
        return success
    }

    fun updateProfile(profile: Profile): Boolean {
        val evaluatedProfile = evaluator.evaluate(profile)
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
        val updatedProfiles = evaluator.updateProfilesIfNeeded(profiles)

        var hasChanges = false
        profiles.zip(updatedProfiles).forEach { (old, new) ->
            if (old != new) {
                service.replaceProfile(old, new)
                hasChanges = true
            }
        }

        if (hasChanges) {
            saveProfiles()
            Log.d(TAG, "Updated profiles with missing data")
        }
    }

    private fun saveProfiles() {
        repository.saveProfiles(service.getAllProfiles(), service.getCurrentProfileId())
    }
}