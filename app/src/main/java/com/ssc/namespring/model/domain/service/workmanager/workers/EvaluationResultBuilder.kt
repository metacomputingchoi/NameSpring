// model/domain/service/workmanager/workers/EvaluationResultBuilder.kt
package com.ssc.namespring.model.domain.service.workmanager.workers

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.ssc.namespring.model.domain.service.theme.ThemeService
import com.ssc.namingengine.data.GeneratedName

internal class EvaluationResultBuilder(
    private val context: Context
) {

    companion object {
        private const val TAG = "EvaluationResultBuilder"
    }

    private val gson = Gson()
    private val themeService = ThemeService(context)

    data class Result(
        val data: Map<String, Any>,
        val rawDataJson: String?
    )

    fun buildResult(evaluatedName: GeneratedName, namebomScore: Int): Result {
        Log.d(TAG, "Building evaluation result...")

        // Get theme information
        val theme = themeService.getThemeByScore(namebomScore)
        Log.d(TAG, "Theme: ${theme.name}")

        // Build result map
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

        // Add analysis info if available
        evaluatedName.analysisInfo?.let { info ->
            Log.d(TAG, "Adding analysis info to result")
            evaluationResult["analysisInfo"] = mapOf(
                "eumYangPattern" to info.eumYangInfo.pattern,
                "eumYangBalance" to info.eumYangInfo.isBalanced,
                "ohaengHarmony" to info.ohaengInfo.overallHarmony,
                "scoreBreakdown" to info.scoreBreakdown
            )
        }

        // Serialize GeneratedName
        Log.d(TAG, "Serializing GeneratedName...")
        val rawDataJson = try {
            gson.toJson(evaluatedName)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to serialize GeneratedName", e)
            null
        }

        return Result(evaluationResult, rawDataJson)
    }
}