// model/domain/usecase/profile/ProfileInitializer.kt
package com.ssc.namespring.model.domain.usecase.profile

import android.util.Log
import com.ssc.namespring.model.domain.service.interfaces.IProfileService
import com.ssc.namespring.model.domain.service.interfaces.IProfileRepository
import com.ssc.namespring.model.domain.service.interfaces.IProfileMigrator
import com.google.gson.JsonSyntaxException

/**
 * 프로필 초기화를 담당하는 클래스
 */
internal class ProfileInitializer(
    private val repository: IProfileRepository,
    private val service: IProfileService,
    private val migrator: IProfileMigrator,
    private val evaluationHandler: ProfileEvaluationHandler
) {
    companion object {
        private const val TAG = "ProfileInitializer"
    }

    fun initialize() {
        loadProfiles()
        evaluationHandler.updateProfilesIfNeeded()
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
            ProfilePersistenceHandler(repository, service).saveProfiles()
            Log.i(TAG, "Successfully migrated ${migratedProfiles.size} legacy profiles")
        } else {
            repository.clearProfiles()
            service.initProfiles(emptyList(), null)
            Log.w(TAG, "Failed to migrate legacy profiles, starting fresh")
        }
    }
}