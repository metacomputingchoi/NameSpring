// ProfileManager.kt
package com.ssc.namespring.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.Calendar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namingengine.NamingEngine
import java.time.ZoneId
import com.ssc.namespring.model.data.SajuInfo
import com.ssc.namespring.model.data.Pillar
import com.google.gson.JsonSyntaxException
import com.ssc.namespring.model.utils.SajuEvaluator

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

        // NamingEngine 초기화
        try {
            namingEngine = NamingEngine.create()
            Log.d(TAG, "NamingEngine 초기화 성공")
        } catch (e: Exception) {
            Log.e(TAG, "NamingEngine 초기화 실패", e)
            return
        }

        loadProfiles()

        // 기존 프로필들 재평가 - 오행 정보가 없는 프로필만
        if (::namingEngine.isInitialized) {
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
    }

    // evaluateProfile 메서드 수정
    private fun evaluateProfile(profile: Profile) {
        if (!::namingEngine.isInitialized) {
            Log.w(TAG, "NamingEngine이 초기화되지 않아 평가를 건너뜁니다")
            return
        }

        // 이름 정보가 완전한지 확인
        val hasCompleteName = profile.givenName?.let { givenName ->
            givenName.charInfos.isNotEmpty() &&
                    givenName.charInfos.all { it.korean.isNotEmpty() && it.hanja.isNotEmpty() }
        } ?: false

        val hasSurname = profile.surname != null

        Log.d(TAG, "프로필 평가 - ID: ${profile.id}, 완전한 이름: $hasCompleteName, 성씨: $hasSurname")

        if (hasCompleteName && hasSurname) {
            // 전체 평가 (이름 + 사주)
            evaluateFullProfile(profile)
        } else {
            // 사주만 평가하여 profiles 리스트의 프로필 직접 업데이트
            val updatedProfile = SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
            val index = profiles.indexOfFirst { it.id == profile.id }
            if (index != -1) {
                profiles[index] = updatedProfile
                Log.d(TAG, "프로필 사주 평가 완료 - ID: ${profile.id}, 오행: ${updatedProfile.ohaengInfo}")
            }
        }
    }

    // evaluateFullProfile 메서드 수정
    private fun evaluateFullProfile(profile: Profile) {
        profile.givenName?.let { givenName ->
            profile.surname?.let { surname ->
                val nameInput = buildNameInput(surname, givenName)
                Log.d(TAG, "전체 평가 시작 - 이름: $nameInput")

                try {
                    val birthDateTime = profile.birthDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()

                    val evaluatedNames = namingEngine.generateNames(
                        userInput = nameInput,
                        birthDateTime = birthDateTime,
                        useYajasi = profile.isYajaTime,
                        verbose = false,
                        withoutFilter = true
                    )

                    evaluatedNames.firstOrNull()?.let { generatedName ->
                        // updateFromGeneratedName을 호출한 후 반환된 값으로 profiles 업데이트
                        profile.updateFromGeneratedName(generatedName)

                        val index = profiles.indexOfFirst { it.id == profile.id }
                        if (index != -1) {
                            profiles[index] = profile
                            Log.d(TAG, "전체 평가 완료 - 점수: ${profile.nameBomScore}, 오행: ${profile.ohaengInfo}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "전체 평가 실패 - ID: ${profile.id}", e)
                    // 실패 시 사주만이라도 평가
                    val updatedProfile = SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
                    val index = profiles.indexOfFirst { it.id == profile.id }
                    if (index != -1) {
                        profiles[index] = updatedProfile
                    }
                }
            }
        }
    }

    fun addProfile(profile: Profile): Boolean {
        if (isDuplicateProfile(profile)) {
            Log.w(TAG, "중복된 프로필 추가 시도")
            return false
        }

        var profileToAdd = profile

        // 무조건 사주는 평가 (생년월일시 정보는 항상 있음)
        if (::namingEngine.isInitialized) {
            Log.d(TAG, "프로필 평가 시작 - ${profile.profileName}")

            try {
                // 이름 정보 확인
                val hasCompleteName = profile.givenName?.let { givenName ->
                    givenName.charInfos.isNotEmpty() &&
                            givenName.charInfos.all { it.korean.isNotEmpty() && it.hanja.isNotEmpty() }
                } ?: false

                if (hasCompleteName && profile.surname != null) {
                    // 전체 평가
                    val birthDateTime = profile.birthDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()

                    val nameInput = buildNameInput(profile.surname!!, profile.givenName!!)

                    val evaluatedNames = namingEngine.generateNames(
                        userInput = nameInput,
                        birthDateTime = birthDateTime,
                        useYajasi = profile.isYajaTime,
                        verbose = false,
                        withoutFilter = true
                    )

                    evaluatedNames.firstOrNull()?.let { generatedName ->
                        profileToAdd.updateFromGeneratedName(generatedName)
                        Log.d(TAG, "전체 평가 완료 - 점수: ${profileToAdd.nameBomScore}, 오행: ${profileToAdd.ohaengInfo}")
                    } ?: run {
                        // 전체 평가 실패 시 사주만 평가
                        Log.w(TAG, "전체 평가 실패, 사주만 평가")
                        profileToAdd = SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
                    }
                } else {
                    // 사주만 평가
                    profileToAdd = SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
                    Log.d(TAG, "사주만 평가 완료 - 오행: ${profileToAdd.ohaengInfo}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "프로필 평가 중 오류", e)
                // 최소한 사주라도 평가 시도
                try {
                    profileToAdd = SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
                } catch (e2: Exception) {
                    Log.e(TAG, "사주 평가도 실패", e2)
                }
            }
        } else {
            Log.e(TAG, "NamingEngine이 초기화되지 않아 평가 불가")
        }

        // 평가된 프로필 추가
        profiles.add(profileToAdd)
        if (profiles.size == 1) {
            currentProfileId = profileToAdd.id
        }
        saveProfiles()

        Log.d(TAG, "프로필 추가 완료 - ID: ${profileToAdd.id}, " +
                "점수: ${profileToAdd.nameBomScore}, " +
                "오행: ${profileToAdd.ohaengInfo}")

        return true
    }

    // ProfileManager의 deleteProfiles 메서드도 수정
    fun deleteProfiles(profileIds: List<String>) {
        profiles.removeIf { it.id in profileIds }

        // 현재 프로필이 삭제되었으면 currentProfileId 초기화
        if (profileIds.contains(currentProfileId)) {
            currentProfileId = profiles.firstOrNull()?.id
        }

        saveProfiles()
        Log.d(TAG, "${profileIds.size}개 프로필 삭제 완료")
    }

    private fun buildNameInput(surname: SurnameInfo, givenName: GivenNameInfo): String {
        val surnameInput = "[${surname.korean}/${surname.hanja}]"
        val givenNameInput = givenName.charInfos.joinToString("") { charInfo ->
            "[${charInfo.korean}/${charInfo.hanja}]"
        }
        return surnameInput + givenNameInput
    }

    fun updateProfile(profile: Profile): Boolean {
        if (isDuplicateProfile(profile)) {
            return false
        }

        // 프로필 업데이트 시 재평가
        var profileToUpdate = profile.copy(updatedAt = System.currentTimeMillis())

        if (::namingEngine.isInitialized) {
            evaluateProfile(profileToUpdate)
        }

        val index = profiles.indexOfFirst { it.id == profile.id }
        if (index != -1) {
            profiles[index] = profileToUpdate
            saveProfiles()
            Log.d(TAG, "프로필 업데이트 완료 - ID: ${profile.id}")
            return true
        }
        return false
    }

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

    // 이전 버전 호환성을 위한 코드들
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

    private fun loadProfiles() {
        val json = sharedPreferences.getString(KEY_PROFILES, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<Profile>>() {}.type
                profiles.clear()
                profiles.addAll(gson.fromJson(json, type))
            } catch (e: JsonSyntaxException) {
                try {
                    val legacyType = object : TypeToken<List<LegacyProfile>>() {}.type
                    val legacyProfiles: List<LegacyProfile> = gson.fromJson(json, legacyType)

                    profiles.clear()
                    legacyProfiles.forEach { legacy ->
                        val newProfile = Profile(
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
                                    fourPillars = listOf(oldSaju.yearPillar, oldSaju.monthPillar, oldSaju.dayPillar, oldSaju.hourPillar),
                                    yearPillar = Pillar.fromPillarString(oldSaju.yearPillar),
                                    monthPillar = Pillar.fromPillarString(oldSaju.monthPillar),
                                    dayPillar = Pillar.fromPillarString(oldSaju.dayPillar),
                                    hourPillar = Pillar.fromPillarString(oldSaju.hourPillar),
                                    sajuOhaengCount = mapOf("木" to 0, "火" to 0, "土" to 0, "金" to 0, "水" to 0),
                                    missingElements = listOf(),
                                    dominantElements = listOf(),
                                    elementBalance = mapOf("木" to 0.2f, "火" to 0.2f, "土" to 0.2f, "金" to 0.2f, "水" to 0.2f)
                                )
                            },
                            nameCharCount = legacy.nameCharCount,
                            createdAt = legacy.createdAt,
                            updatedAt = legacy.updatedAt
                        )
                        profiles.add(newProfile)
                    }

                    saveProfiles()
                    Log.i(TAG, "레거시 프로필 ${profiles.size}개 마이그레이션 완료")
                } catch (e2: Exception) {
                    Log.e(TAG, "레거시 프로필 마이그레이션 실패", e2)
                    profiles.clear()
                    sharedPreferences.edit().remove(KEY_PROFILES).apply()
                }
            }
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