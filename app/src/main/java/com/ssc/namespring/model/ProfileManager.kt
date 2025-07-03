// com/ssc/namespring/model/ProfileManager.kt
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

        // 임시로 사주/오행 정보 생성
        profiles.forEach { profile ->
            if (profile.sajuInfo == null) {
                profile.sajuInfo = generateTempSajuInfo()
            }
            if (profile.ohaengInfo == null) {
                profile.ohaengInfo = generateTempOhaengInfo()
            }
            if (profile.nameBomScore == 0) {
                profile.nameBomScore = (20..95).random()
            }
        }
        saveProfiles()
    }

    private fun generateTempSajuInfo(): SajuInfo {
        val pillars = listOf("甲子", "乙丑", "丙寅", "丁卯", "戊辰", "己巳", "庚午", "辛未", "壬申", "癸酉", "甲戌", "乙亥")
        return SajuInfo(
            yearPillar = pillars.random(),
            monthPillar = pillars.random(),
            dayPillar = pillars.random(),
            hourPillar = pillars.random()
        )
    }

    private fun generateTempOhaengInfo(): OhaengInfo {
        return OhaengInfo(
            wood = (0..5).random(),
            fire = (0..5).random(),
            earth = (0..5).random(),
            metal = (0..5).random(),
            water = (0..5).random()
        )
    }

    // 중복 체크 메서드
    fun isDuplicateProfile(profile: Profile): Boolean {
        return profiles.any { it.equals(profile) && it.id != profile.id }
    }

    // 검색 메서드
    fun searchProfiles(query: String): List<Profile> {
        if (query.isEmpty()) return getAllProfiles()

        val lowercaseQuery = query.lowercase()
        return profiles.filter { profile ->
            profile.profileName.lowercase().contains(lowercaseQuery) ||
                    profile.getFullName().lowercase().contains(lowercaseQuery) ||
                    profile.getFullNameWithHanja().lowercase().contains(lowercaseQuery) ||
                    profile.getBirthDateString().contains(query)
        }
    }

    // 정렬 메서드
    fun getSortedProfiles(sortType: SortType): List<Profile> {
        return when (sortType) {
            SortType.NAME_ASC -> profiles.sortedBy { it.profileName }
            SortType.NAME_DESC -> profiles.sortedByDescending { it.profileName }
            SortType.SCORE_DESC -> profiles.sortedByDescending { it.nameBomScore }
            SortType.SCORE_ASC -> profiles.sortedBy { it.nameBomScore }
            SortType.DATE_DESC -> profiles.sortedByDescending { it.createdAt }
            SortType.DATE_ASC -> profiles.sortedBy { it.createdAt }
        }
    }

    // 다중 삭제
    fun deleteProfiles(profileIds: List<String>) {
        profiles.removeIf { it.id in profileIds }
        saveProfiles()
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

    fun addProfile(profile: Profile): Boolean {
        if (isDuplicateProfile(profile)) {
            return false
        }
        profiles.add(profile)
        if (profiles.size == 1) {
            currentProfileId = profile.id
        }
        saveProfiles()
        return true
    }

    fun updateProfile(profile: Profile): Boolean {
        if (isDuplicateProfile(profile)) {
            return false
        }
        val index = profiles.indexOfFirst { it.id == profile.id }
        if (index != -1) {
            profiles[index] = profile.copy(updatedAt = System.currentTimeMillis())
            saveProfiles()
            return true
        }
        return false
    }

    fun deleteProfile(profileId: String) {
        profiles.removeIf { it.id == profileId }
        saveProfiles()
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

    enum class SortType {
        NAME_ASC, NAME_DESC, SCORE_DESC, SCORE_ASC, DATE_DESC, DATE_ASC
    }
}