// model/ProfileManager.kt
package com.ssc.namespring.model

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ProfileManager {
    private const val PREF_NAME = "namespring_profiles"
    private const val KEY_PROFILES = "profiles"
    private const val KEY_CURRENT_PROFILE_ID = "current_profile_id"

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    private val profiles = mutableListOf<Profile>()
    private var currentProfileId: String? = null
    private var selectedProfile: Profile? = null

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadProfiles()
    }

    private fun loadProfiles() {
        val json = sharedPreferences.getString(KEY_PROFILES, null)
        if (json != null) {
            val type = object : TypeToken<List<Profile>>() {}.type
            profiles.clear()
            profiles.addAll(gson.fromJson(json, type))
        }
        currentProfileId = sharedPreferences.getString(KEY_CURRENT_PROFILE_ID, null)
    }

    private fun saveProfiles() {
        val json = gson.toJson(profiles)
        sharedPreferences.edit()
            .putString(KEY_PROFILES, json)
            .putString(KEY_CURRENT_PROFILE_ID, currentProfileId)
            .apply()
    }

    fun getAllProfiles(): List<Profile> = profiles.toList()

    fun getProfile(id: String): Profile? = profiles.find { it.id == id }

    fun getCurrentProfile(): Profile? = currentProfileId?.let { getProfile(it) }

    fun addProfile(profile: Profile) {
        profiles.add(profile)
        if (profiles.size == 1) {
            currentProfileId = profile.id
        }
        saveProfiles()
    }

    fun updateProfile(profile: Profile) {
        val index = profiles.indexOfFirst { it.id == profile.id }
        if (index != -1) {
            profiles[index] = profile
            saveProfiles()
        }
    }

    fun switchProfile(id: String) {
        if (profiles.any { it.id == id }) {
            currentProfileId = id
            saveProfiles()
        }
    }

    fun hasProfiles(): Boolean = profiles.isNotEmpty()


    fun setSelectedProfile(profile: Profile) {
        selectedProfile = profile
    }

    fun getSelectedProfile(): Profile? {
        return selectedProfile
    }

    fun deleteProfile(profileId: String) {
        profiles.removeIf { it.id == profileId }
        saveProfiles()
    }
}