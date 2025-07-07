// model/domain/service/workmanager/workers/EvaluationWorker.kt
package com.ssc.namespring.model.domain.service.workmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.ssc.namespring.model.domain.service.workmanager.BaseWorker
import com.ssc.namespring.model.domain.service.factory.NamingEngineProvider
import com.ssc.namespring.model.domain.service.evaluation.ProfileScoreCalculator
import com.ssc.namespring.model.domain.service.theme.ThemeService
import com.ssc.namingengine.data.GeneratedName
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class EvaluationWorker(
    context: Context,
    params: WorkerParameters
) : BaseWorker(context, params) {

    companion object {
        private const val TAG = "EvaluationWorker"
    }

    private val localGson = Gson()

    override suspend fun performWork(): WorkResult {
        try {
            Log.d(TAG, "Starting evaluation work for profile: $profileId")

            val inputData = getInputDataMap()
            Log.d(TAG, "Input data keys: ${inputData.keys}")
            Log.d(TAG, "Full input data: $inputData")

            // Extract evaluation parameters
            val birthDateTimeMillis = (inputData["birthDateTime"] as? String)?.toLongOrNull()
            Log.d(TAG, "Birth date time millis: $birthDateTimeMillis")

            if (birthDateTimeMillis == null) {
                return WorkResult(
                    success = false,
                    error = "Birth date time is required"
                )
            }

            val isYajaTime = inputData["isYajaTime"] as? Boolean ?: true
            Log.d(TAG, "IsYajaTime: $isYajaTime")

            // Handle Map casting carefully
            val surnameData = inputData["surname"] as? Map<*, *>
            val givenNameData = inputData["givenName"] as? Map<*, *>

            Log.d(TAG, "Raw surname data: $surnameData")
            Log.d(TAG, "Raw given name data: $givenNameData")

            updateProgress(10)

            // Build full name input with safe casting
            val surnameKorean = surnameData?.get("korean")?.toString() ?: ""
            val surnameHanja = surnameData?.get("hanja")?.toString() ?: ""
            val givenNameKorean = givenNameData?.get("korean")?.toString() ?: ""
            val givenNameHanja = givenNameData?.get("hanja")?.toString() ?: ""

            Log.d(TAG, "Extracted name components:")
            Log.d(TAG, "  Surname Korean: '$surnameKorean'")
            Log.d(TAG, "  Surname Hanja: '$surnameHanja'")
            Log.d(TAG, "  Given name Korean: '$givenNameKorean'")
            Log.d(TAG, "  Given name Hanja: '$givenNameHanja'")

            if (surnameKorean.isEmpty() || surnameHanja.isEmpty() ||
                givenNameKorean.isEmpty() || givenNameHanja.isEmpty()) {
                return WorkResult(
                    success = false,
                    error = "Complete name information is required (Korean: $surnameKorean/$givenNameKorean, Hanja: $surnameHanja/$givenNameHanja)"
                )
            }

            // Build evaluation input
            val evaluationInput = "[$surnameKorean/$surnameHanja]" +
                    givenNameKorean.toCharArray().zip(givenNameHanja.toCharArray()).joinToString("") { (k, h) ->
                        "[$k/$h]"
                    }

            Log.d(TAG, "Built evaluation input: $evaluationInput")

            updateProgress(20)

            // Convert timestamp to LocalDateTime
            val birthDateTime = try {
                Instant.ofEpochMilli(birthDateTimeMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to convert birth date time", e)
                return WorkResult(
                    success = false,
                    error = "Invalid birth date time: ${e.message}"
                )
            }

            Log.d(TAG, "Converted birth date time: $birthDateTime")

            // Initialize naming engine
            Log.d(TAG, "Getting naming engine instance...")
            val namingEngine = try {
                NamingEngineProvider.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get naming engine", e)
                return WorkResult(
                    success = false,
                    error = "Failed to initialize naming engine: ${e.message}"
                )
            }

            updateProgress(30)

            // Evaluate the name
            Log.d(TAG, "Calling naming engine generateNames...")
            val evaluatedNames = try {
                namingEngine.generateNames(
                    userInput = evaluationInput,
                    birthDateTime = birthDateTime,
                    useYajasi = isYajaTime,
                    verbose = true,
                    withoutFilter = true  // 필터링 없이 입력한 이름 그대로 평가
                )
            } catch (e: Exception) {
                Log.e(TAG, "Naming engine generateNames failed", e)
                return WorkResult(
                    success = false,
                    error = "Failed to evaluate name: ${e.message}"
                )
            }

            Log.d(TAG, "Generated ${evaluatedNames.size} names")

            updateProgress(70)

            if (evaluatedNames.isEmpty()) {
                Log.e(TAG, "No evaluation result returned")
                return WorkResult(
                    success = false,
                    error = "No evaluation result"
                )
            }

            val evaluatedName = evaluatedNames.first()
            Log.d(TAG, "First evaluated name: ${evaluatedName.combinedPronounciation}(${evaluatedName.combinedHanja})")

            // Calculate scores
            Log.d(TAG, "Calculating namebom score...")
            val namebomScore = try {
                ProfileScoreCalculator.calculateNamebomScore(evaluatedName)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to calculate namebom score", e)
                0
            }
            Log.d(TAG, "Namebom score: $namebomScore")

            // Get theme information
            Log.d(TAG, "Getting theme information...")
            val themeService = ThemeService(applicationContext)
            val theme = themeService.getThemeByScore(namebomScore)
            Log.d(TAG, "Theme: ${theme.name}")

            updateProgress(90)

            // Prepare evaluation result
            val evaluationResult = mutableMapOf<String, Any>(
                "namebomScore" to namebomScore,
                "totalScore" to (evaluatedName.analysisInfo?.totalScore ?: 0),
                "themeName" to theme.name,
                "themeDescription" to theme.description,
                "sagyeok" to mapOf(
                    "hyeong" to evaluatedName.sagyeok.hyeong,
                    "won" to evaluatedName.sagyeok.won,
                    "i" to evaluatedName.sagyeok.i,
                    "jeong" to evaluatedName.sagyeok.jeong
                ),
                "evaluatedAt" to System.currentTimeMillis()
            )

            // analysisInfo가 있을 때만 추가
            evaluatedName.analysisInfo?.let { info ->
                Log.d(TAG, "Adding analysis info to result")
                evaluationResult["analysisInfo"] = mapOf(
                    "eumYangPattern" to info.eumYangInfo.pattern,
                    "eumYangBalance" to info.eumYangInfo.isBalanced,
                    "ohaengHarmony" to info.ohaengInfo.overallHarmony,
                    "scoreBreakdown" to info.scoreBreakdown
                )
            }

            // Serialize the GeneratedName for storage
            Log.d(TAG, "Serializing GeneratedName...")
            val rawDataJson = try {
                localGson.toJson(evaluatedName)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to serialize GeneratedName", e)
                null
            }

            updateProgress(100)

            Log.d(TAG, "Evaluation completed successfully")
            return WorkResult(
                success = true,
                data = evaluationResult,
                rawData = rawDataJson
            )
        } catch (e: Exception) {
            Log.e(TAG, "Evaluation failed with exception", e)
            return WorkResult(
                success = false,
                error = "Evaluation failed: ${e.message}"
            )
        }
    }
}