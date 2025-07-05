// model/data/mapper/ProfileMigrator.kt
package com.ssc.namespring.model.data.mapper

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.entity.OhaengInfo
import com.ssc.namespring.model.domain.entity.Pillar
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SajuInfo
import com.ssc.namespring.model.domain.entity.SurnameInfo
import java.util.Calendar

internal class ProfileMigrator(private val gson: Gson = Gson()) {
    companion object {
        private const val TAG = "ProfileMigrator"
    }

    fun migrateFromJson(json: String): List<Profile>? {
        return try {
            val legacyType = object : TypeToken<List<LegacyProfile>>() {}.type
            val legacyProfiles: List<LegacyProfile> = gson.fromJson(json, legacyType)

            val migratedProfiles = legacyProfiles.map { convertLegacyProfile(it) }
            Log.i(TAG, "레거시 프로필 ${migratedProfiles.size}개 마이그레이션 완료")
            migratedProfiles
        } catch (e: Exception) {
            Log.e(TAG, "레거시 프로필 마이그레이션 실패", e)
            null
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
                        oldSaju.yearPillar, oldSaju.monthPillar,
                        oldSaju.dayPillar, oldSaju.hourPillar
                    ),
                    yearPillar = Pillar.fromPillarString(oldSaju.yearPillar),
                    monthPillar = Pillar.fromPillarString(oldSaju.monthPillar),
                    dayPillar = Pillar.fromPillarString(oldSaju.dayPillar),
                    hourPillar = Pillar.fromPillarString(oldSaju.hourPillar),
                    sajuOhaengCount = mapOf("木" to 0, "火" to 0, "土" to 0, "金" to 0, "水" to 0),
                    missingElements = listOf(),
                    dominantElements = listOf(),
                    elementBalance = mapOf(
                        "木" to 0.2f, "火" to 0.2f, "土" to 0.2f,
                        "金" to 0.2f, "水" to 0.2f
                    )
                )
            },
            nameCharCount = legacy.nameCharCount,
            createdAt = legacy.createdAt,
            updatedAt = legacy.updatedAt
        )
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