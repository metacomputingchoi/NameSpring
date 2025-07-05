// model/repository/ProfileRepository.kt
package com.ssc.namespring.model.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.data.Profile

internal class ProfileRepository(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson = Gson()
) {
    companion object {
        private const val KEY_PROFILES = "profiles"
        private const val KEY_CURRENT_PROFILE_ID = "current_profile_id"
    }

    fun saveProfiles(profiles: List<Profile>, currentProfileId: String?) {
        val json = gson.toJson(profiles)
        sharedPreferences.edit()
            .putString(KEY_PROFILES, json)
            .putString(KEY_CURRENT_PROFILE_ID, currentProfileId)
            .apply()
    }

    fun loadProfiles(): List<Profile> {
        val json = sharedPreferences.getString(KEY_PROFILES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Profile>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadProfilesJson(): String? {
        return sharedPreferences.getString(KEY_PROFILES, null)
    }

    fun loadCurrentProfileId(): String? {
        return sharedPreferences.getString(KEY_CURRENT_PROFILE_ID, null)
    }

    fun clearProfiles() {
        sharedPreferences.edit().remove(KEY_PROFILES).apply()
    }
}