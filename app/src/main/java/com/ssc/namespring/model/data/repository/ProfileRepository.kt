// model/data/repository/ProfileRepository.kt
package com.ssc.namespring.model.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.domain.entity.Profile
import androidx.core.content.edit

internal class ProfileRepository(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson = Gson()
) {
    companion object {
        private const val TAG = "ProfileRepository"
        private const val KEY_PROFILES = "profiles"
        private const val KEY_CURRENT_PROFILE_ID = "current_profile_id"
    }

    fun saveProfiles(profiles: List<Profile>, currentProfileId: String?) {
        Log.d(TAG, "저장할 프로필 수: ${profiles.size}")
        profiles.forEach { profile ->
            Log.d(TAG, "프로필 ${profile.id}:")
            Log.d(TAG, "  - profileName: ${profile.profileName}")
            Log.d(TAG, "  - nameBomScore: ${profile.nameBomScore}")
            Log.d(TAG, "  - evaluatedNameJson 길이: ${profile.evaluatedNameJson?.length}")
            Log.d(TAG, "  - evaluatedName 존재: ${profile.evaluatedName != null}")
        }

        val json = gson.toJson(profiles)
        Log.d(TAG, "생성된 JSON 길이: ${json.length}")

        sharedPreferences.edit {
            putString(KEY_PROFILES, json)
            putString(KEY_CURRENT_PROFILE_ID, currentProfileId)
        }

        Log.d(TAG, "SharedPreferences에 저장 완료")
    }

    fun loadProfiles(): List<Profile> {
        val json = sharedPreferences.getString(KEY_PROFILES, null) ?: return emptyList()

        Log.d(TAG, "불러온 JSON 길이: ${json.length}")

        return try {
            val type = object : TypeToken<List<Profile>>() {}.type
            val profiles: List<Profile> = gson.fromJson(json, type)

            Log.d(TAG, "불러온 프로필 수: ${profiles.size}")
            profiles.forEach { profile ->
                Log.d(TAG, "프로필 ${profile.id}:")
                Log.d(TAG, "  - profileName: ${profile.profileName}")
                Log.d(TAG, "  - nameBomScore: ${profile.nameBomScore}")
                Log.d(TAG, "  - evaluatedNameJson 길이: ${profile.evaluatedNameJson?.length}")
                // evaluatedName은 getter를 통해 자동으로 복원됨
                Log.d(TAG, "  - evaluatedName 복원: ${profile.evaluatedName != null}")
            }

            profiles
        } catch (e: Exception) {
            Log.e(TAG, "프로필 로드 실패", e)
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
        sharedPreferences.edit {
            remove(KEY_PROFILES)
            remove(KEY_CURRENT_PROFILE_ID)
        }
        Log.d(TAG, "프로필 데이터 삭제됨")
    }
}