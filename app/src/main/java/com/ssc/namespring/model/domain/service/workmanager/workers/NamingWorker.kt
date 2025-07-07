// model/domain/service/workmanager/workers/NamingWorker.kt
package com.ssc.namespring.model.domain.service.workmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.ssc.namespring.model.domain.service.workmanager.BaseWorker
import com.ssc.namespring.model.domain.service.factory.NamingEngineProvider
import com.ssc.namespring.model.domain.service.evaluation.ProfileScoreCalculator
import com.ssc.namingengine.data.GeneratedName
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class NamingWorker(
    context: Context,
    params: WorkerParameters
) : BaseWorker(context, params) {
    companion object {
        private const val TAG = "NamingWorker"
    }

    private val localGson = Gson()

    override suspend fun performWork(): WorkResult {
        try {
            Log.d(TAG, "Starting naming work for profile: $profileId")

            val inputData = getInputDataMap()

            // Extract naming parameters
            val birthDateTimeMillis = (inputData["birthDateTime"] as? String)?.toLongOrNull()
                ?: return WorkResult(
                    success = false,
                    error = "Birth date time is required"
                )

            val isYajaTime = inputData["isYajaTime"] as? Boolean ?: true
            val surnameData = inputData["surname"] as? Map<*, *>
            val nameInputFormat = inputData["nameInputFormat"] as? String ?: ""
            val nameCharCount = (inputData["nameCharCount"] as? Double)?.toInt() ?: 2

            updateProgress(10)

            // Convert timestamp to LocalDateTime
            val birthDateTime = Instant.ofEpochMilli(birthDateTimeMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            // Initialize naming engine
            val namingEngine = NamingEngineProvider.getInstance()

            updateProgress(20)

            // Build surname input string
            val surnameKorean = surnameData?.get("korean")?.toString() ?: ""
            val surnameHanja = surnameData?.get("hanja")?.toString() ?: ""

            // ProfileFormActivity와 동일한 형식 사용
            val surnameInput = if (surnameKorean.isNotEmpty() && surnameHanja.isNotEmpty()) {
                "[$surnameKorean/$surnameHanja]"
            } else {
                return WorkResult(
                    success = false,
                    error = "Surname information is required"
                )
            }

            // 이름 부분 추가 (비어있으면 [_/_] 형식)
            val fullInput = if (nameInputFormat.isNotEmpty()) {
                surnameInput + nameInputFormat
            } else {
                // nameCharCount에 따라 [_/_] 추가
                surnameInput + "[_/_]".repeat(nameCharCount)
            }

            Log.d(TAG, "Full naming input: $fullInput")

            updateProgress(30)

            // Generate names using NamingEngine
            val generatedNames = try {
                namingEngine.generateNames(
                    userInput = fullInput,
                    birthDateTime = birthDateTime,
                    useYajasi = isYajaTime,
                    verbose = true,
                    withoutFilter = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate names", e)
                return WorkResult(
                    success = false,
                    error = "Failed to generate names: ${e.message}"
                )
            }

            Log.d(TAG, "Generated ${generatedNames.size} names")

            updateProgress(70)

            // Take top 10 names for summary
            val topNames = generatedNames.take(10)

            // Convert to summary format
            val nameSuggestions = topNames.map { generatedName ->
                val score = ProfileScoreCalculator.calculateNamebomScore(generatedName)
                val givenNameHanja = generatedName.combinedHanja.substring(generatedName.surnameHanja.length)
                val givenNamePronunciation = generatedName.combinedPronounciation.substring(generatedName.surnameHangul.length)

                mapOf(
                    "korean" to givenNamePronunciation,
                    "hanja" to givenNameHanja,
                    "fullKorean" to generatedName.combinedPronounciation,
                    "fullHanja" to generatedName.combinedHanja,
                    "meaning" to buildMeaningString(generatedName),
                    "score" to score,
                    "totalScore" to (generatedName.analysisInfo?.totalScore ?: 0)
                )
            }

            updateProgress(90)

            // Serialize all GeneratedName objects for storage
            val rawDataJson = localGson.toJson(generatedNames)

            updateProgress(100)

            return WorkResult(
                success = true,
                data = mapOf(
                    "suggestions" to nameSuggestions,
                    "totalCount" to generatedNames.size,
                    "topCount" to topNames.size,
                    "timestamp" to System.currentTimeMillis(),
                    "parameters" to mapOf(
                        "surname" to surnameInput,
                        "fullInput" to fullInput,
                        "birthDateTime" to birthDateTime.toString(),
                        "isYajaTime" to isYajaTime
                    )
                ),
                rawData = rawDataJson
            )
        } catch (e: Exception) {
            Log.e(TAG, "Naming failed", e)
            return WorkResult(
                success = false,
                error = "Naming failed: ${e.message}"
            )
        }
    }

    private fun buildMeaningString(generatedName: GeneratedName): String {
        return generatedName.hanjaDetails
            .drop(1) // Skip surname
            .mapNotNull { it.inmyongMeaning.takeIf { meaning -> meaning.isNotBlank() } }
            .joinToString(", ")
            .ifEmpty { "의미 정보 없음" }
    }
}