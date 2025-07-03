// model/repository/ProfileManager.kt
package com.ssc.namespring.model.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.data.GivenNameInfo
import com.ssc.namespring.model.data.OhaengInfo
import com.ssc.namingengine.NamingEngine
import com.ssc.namespring.model.data.SajuInfo
import com.ssc.namespring.model.data.Pillar
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.data.SurnameInfo
import com.ssc.namespring.model.utils.SajuEvaluator
import java.time.ZoneId
import java.util.Calendar

object ProfileManager {
    private const val TAG = "ProfileManager"
    private const val PREF_NAME = "namespring_profiles"
    private const val KEY_PROFILES = "profiles"
    private const val KEY_CURRENT_PROFILE_ID = "current_profile_id"

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var namingEngine: NamingEngine
    private val gson = Gson()

    private val profiles = mutableListOf<Profile>()
    private var currentProfileId: String? = null
    private var selectedProfile: Profile? = null

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        try {
            namingEngine = NamingEngine.create()
            Log.d(TAG, "NamingEngine 초기화 성공")
        } catch (e: Exception) {
            Log.e(TAG, "NamingEngine 초기화 실패", e)
            return
        }

        loadProfiles()
        updateProfilesIfNeeded()
    }

    private fun updateProfilesIfNeeded() {
        if (!::namingEngine.isInitialized) return

        var updatedCount = 0
        val profilesToUpdate = profiles.filter { it.ohaengInfo == null }

        Log.d(TAG, "재평가 필요한 프로필: ${profilesToUpdate.size}개")

        profilesToUpdate.forEach { profile ->
            Log.d(TAG, "프로필 재평가 시작: ${profile.profileName}")

            val updatedProfile = SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
            val index = profiles.indexOfFirst { it.id == profile.id }
            if (index != -1) {
                profiles[index] = updatedProfile
                updatedCount++
                Log.d(TAG, "프로필 재평가 완료: ${profile.profileName}, 오행: ${updatedProfile.ohaengInfo}")
            }
        }

        if (updatedCount > 0) {
            Log.d(TAG, "$updatedCount 개의 프로필 사주 정보 업데이트 완료")
            saveProfiles()
        }
    }

    fun addProfile(profile: Profile): Boolean {
        if (isDuplicateProfile(profile)) {
            Log.w(TAG, "중복된 프로필 추가 시도")
            return false
        }

        val profileToAdd = evaluateProfile(profile)
        profiles.add(profileToAdd)

        if (profiles.size == 1) {
            currentProfileId = profileToAdd.id
        }

        saveProfiles()
        Log.d(TAG, "프로필 추가 완료 - ID: ${profileToAdd.id}")
        return true
    }

    fun updateProfile(profile: Profile): Boolean {
        if (isDuplicateProfile(profile)) {
            return false
        }

        val profileToUpdate = evaluateProfile(profile.copy(updatedAt = System.currentTimeMillis()))

        val index = profiles.indexOfFirst { it.id == profile.id }
        if (index != -1) {
            profiles[index] = profileToUpdate
            saveProfiles()
            Log.d(TAG, "프로필 업데이트 완료 - ID: ${profile.id}")
            return true
        }
        return false
    }

    private fun evaluateProfile(profile: Profile): Profile {
        if (!::namingEngine.isInitialized) {
            Log.w(TAG, "NamingEngine이 초기화되지 않아 평가를 건너뜁니다")
            return profile
        }

        val hasCompleteName = profile.givenName?.let { givenName ->
            givenName.charInfos.isNotEmpty() &&
                    givenName.charInfos.all { it.korean.isNotEmpty() && it.hanja.isNotEmpty() }
        } ?: false

        return if (hasCompleteName && profile.surname != null) {
            evaluateFullProfile(profile)
        } else {
            SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
        }
    }

    private fun evaluateFullProfile(profile: Profile): Profile {
        val givenName = profile.givenName ?: return profile
        val surname = profile.surname ?: return profile

        val nameInput = buildNameInput(surname, givenName)
        Log.d(TAG, "전체 평가 시작 - 이름: $nameInput")

        return try {
            val birthDateTime = profile.birthDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            val evaluatedNames = namingEngine.generateNames(
                userInput = nameInput,
                birthDateTime = birthDateTime,
                useYajasi = profile.isYajaTime,
                verbose = true,
                withoutFilter = true
            )

            evaluatedNames.firstOrNull()?.let { generatedName ->
                profile.apply { updateFromGeneratedName(generatedName) }
            } ?: SajuEvaluator.evaluateProfileSaju(profile, namingEngine)

        } catch (e: Exception) {
            Log.e(TAG, "전체 평가 실패 - ID: ${profile.id}", e)
            SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
        }
    }

    private fun buildNameInput(surname: SurnameInfo, givenName: GivenNameInfo): String {
        val surnameInput = "[${surname.korean}/${surname.hanja}]"
        val givenNameInput = givenName.charInfos.joinToString("") { charInfo ->
            "[${charInfo.korean}/${charInfo.hanja}]"
        }
        return surnameInput + givenNameInput
    }

    fun deleteProfiles(profileIds: List<String>) {
        profiles.removeIf { it.id in profileIds }

        if (profileIds.contains(currentProfileId)) {
            currentProfileId = profiles.firstOrNull()?.id
        }

        saveProfiles()
        Log.d(TAG, "${profileIds.size}개 프로필 삭제 완료")
    }

    fun deleteProfile(profileId: String) = deleteProfiles(listOf(profileId))

    fun isDuplicateProfile(profile: Profile): Boolean {
        return profiles.any { it.equals(profile) && it.id != profile.id }
    }

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

    private fun loadProfiles() {
        val json = sharedPreferences.getString(KEY_PROFILES, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<Profile>>() {}.type
                profiles.clear()
                profiles.addAll(gson.fromJson(json, type))
            } catch (e: JsonSyntaxException) {
                handleLegacyProfiles(json)
            }
        }
        currentProfileId = sharedPreferences.getString(KEY_CURRENT_PROFILE_ID, null)
    }

    private fun handleLegacyProfiles(json: String) {
        try {
            val legacyType = object : TypeToken<List<LegacyProfile>>() {}.type
            val legacyProfiles: List<LegacyProfile> = gson.fromJson(json, legacyType)

            profiles.clear()
            legacyProfiles.forEach { legacy ->
                val newProfile = convertLegacyProfile(legacy)
                profiles.add(newProfile)
            }

            saveProfiles()
            Log.i(TAG, "레거시 프로필 ${profiles.size}개 마이그레이션 완료")
        } catch (e: Exception) {
            Log.e(TAG, "레거시 프로필 마이그레이션 실패", e)
            profiles.clear()
            sharedPreferences.edit().remove(KEY_PROFILES).apply()
        }
    }

    private fun convertLegacyProfile(legacy: LegacyProfile): Profile {
        return Profile(
            id = legacy.id,
            profileName = legacy.profileName,
            birthDate = legacy.birthDate,
            isYajaTime = legacy.isYajaTime,
            surname = legacy.surname,
            givenName = legacy.givenName,
            nameBomScore = legacy.nameBomScore,
            ohaengInfo = legacy.ohaengInfo,
            sajuInfo = legacy.sajuInfo?.let { oldSaju ->
                SajuInfo(
                    fourPillars = listOf(
                        oldSaju.yearPillar,
                        oldSaju.monthPillar,
                        oldSaju.dayPillar,
                        oldSaju.hourPillar
                    ),
                    yearPillar = Pillar.fromPillarString(oldSaju.yearPillar),
                    monthPillar = Pillar.fromPillarString(oldSaju.monthPillar),
                    dayPillar = Pillar.fromPillarString(oldSaju.dayPillar),
                    hourPillar = Pillar.fromPillarString(oldSaju.hourPillar),
                    sajuOhaengCount = mapOf("木" to 0, "火" to 0, "土" to 0, "金" to 0, "水" to 0),
                    missingElements = listOf(),
                    dominantElements = listOf(),
                    elementBalance = mapOf(
                        "木" to 0.2f,
                        "火" to 0.2f,
                        "土" to 0.2f,
                        "金" to 0.2f,
                        "水" to 0.2f
                    )
                )
            },
            nameCharCount = legacy.nameCharCount,
            createdAt = legacy.createdAt,
            updatedAt = legacy.updatedAt
        )
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
    fun switchProfile(id: String) {
        if (profiles.any { it.id == id }) {
            currentProfileId = id
            saveProfiles()
        }
    }
    fun hasProfiles(): Boolean = profiles.isNotEmpty()
    fun setSelectedProfile(profile: Profile) { selectedProfile = profile }
    fun getSelectedProfile(): Profile? = selectedProfile

    enum class SortType {
        NAME_ASC, NAME_DESC, SCORE_DESC, SCORE_ASC, DATE_DESC, DATE_ASC
    }

    private data class LegacySajuInfo(
        val yearPillar: String,
        val monthPillar: String,
        val dayPillar: String,
        val hourPillar: String
    )

    private data class LegacyProfile(
        val id: String,
        val profileName: String,
        val birthDate: Calendar,
        val isYajaTime: Boolean,
        val surname: SurnameInfo?,
        val givenName: GivenNameInfo?,
        val nameBomScore: Int,
        val sajuInfo: LegacySajuInfo?,
        val ohaengInfo: OhaengInfo?,
        val nameCharCount: Int,
        val createdAt: Long,
        val updatedAt: Long
    )
}
