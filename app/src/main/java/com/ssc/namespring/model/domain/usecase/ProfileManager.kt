// model/domain/usecase/ProfileManager.kt
package com.ssc.namespring.model.domain.usecase

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ssc.namingengine.NamingEngine
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.ProfileEvaluator
import com.ssc.namespring.model.data.mapper.ProfileMigrator
import com.ssc.namespring.model.data.repository.ProfileRepository
import com.ssc.namespring.model.domain.service.ProfileService

object ProfileManager {
    private const val TAG = "ProfileManager"
    private const val PREF_NAME = "namespring_profiles"

    private lateinit var repository: ProfileRepository
    private lateinit var service: ProfileService
    private lateinit var evaluator: ProfileEvaluator
    private lateinit var migrator: ProfileMigrator
    private lateinit var namingEngine: NamingEngine
    private val gson = Gson()

    fun init(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        repository = ProfileRepository(sharedPreferences, gson)
        service = ProfileService()
        migrator = ProfileMigrator(gson)

        try {
            namingEngine = NamingEngine.create()
            Log.d(TAG, "NamingEngine 초기화 성공")
        } catch (e: Exception) {
            Log.e(TAG, "NamingEngine 초기화 실패", e)
        }

        evaluator = ProfileEvaluator(namingEngine)
        loadProfiles()
        updateProfilesIfNeeded()
    }

    fun addProfile(profile: Profile): Boolean {
        val evaluatedProfile = evaluator.evaluateProfile(profile)
        val success = service.addProfile(evaluatedProfile)
        if (success) saveProfiles()
        return success
    }

    fun updateProfile(profile: Profile): Boolean {
        val evaluatedProfile = evaluator.evaluateProfile(profile)
        val success = service.updateProfile(evaluatedProfile)
        if (success) saveProfiles()
        return success
    }

    fun deleteProfiles(profileIds: List<String>) {
        service.deleteProfiles(profileIds)
        saveProfiles()
    }

    fun deleteProfile(profileId: String) = deleteProfiles(listOf(profileId))
    fun isDuplicateProfile(profile: Profile): Boolean =
        service.getAllProfiles().any { it.equals(profile) && it.id != profile.id }
    fun searchProfiles(query: String): List<Profile> = service.searchProfiles(query)
    fun getSortedProfiles(sortType: SortType): List<Profile> = service.getSortedProfiles(sortType)
    fun getAllProfiles(): List<Profile> = service.getAllProfiles()
    fun getProfile(id: String): Profile? = service.getProfile(id)
    fun getCurrentProfile(): Profile? = service.getCurrentProfile()
    fun hasProfiles(): Boolean = service.hasProfiles()
    fun setSelectedProfile(profile: Profile) = service.setSelectedProfile(profile)
    fun getSelectedProfile(): Profile? = service.getSelectedProfile()

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
        } else {
            repository.clearProfiles()
            service.initProfiles(emptyList(), null)
        }
    }

    private fun updateProfilesIfNeeded() {
        val profiles = service.getAllProfiles()
        val updatedProfiles = evaluator.updateProfilesIfNeeded(profiles)

        profiles.zip(updatedProfiles).forEach { (old, new) ->
            if (old != new) service.replaceProfile(old, new)
        }

        if (profiles != updatedProfiles) {
            saveProfiles()
        }
    }

    private fun saveProfiles() {
        repository.saveProfiles(service.getAllProfiles(), service.getCurrentProfileId())
    }

    enum class SortType {
        NAME_ASC, NAME_DESC, SCORE_DESC, SCORE_ASC, DATE_DESC, DATE_ASC
    }
}