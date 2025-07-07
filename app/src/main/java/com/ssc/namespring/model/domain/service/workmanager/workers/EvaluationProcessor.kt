// model/domain/service/workmanager/workers/EvaluationProcessor.kt
package com.ssc.namespring.model.domain.service.workmanager.workers

import android.content.Context
import android.util.Log
import com.ssc.namespring.model.domain.service.factory.NamingEngineProvider
import com.ssc.namespring.model.domain.service.evaluation.ProfileScoreCalculator
import com.ssc.namingengine.data.GeneratedName
import java.time.LocalDateTime

internal class EvaluationProcessor(
    private val context: Context
) {

    companion object {
        private const val TAG = "EvaluationProcessor"
    }

    data class ProcessResult(
        val success: Boolean,
        val errorMessage: String? = null,
        val evaluatedName: GeneratedName? = null,
        val namebomScore: Int = 0
    )

    suspend fun processEvaluation(
        evaluationInput: String,
        birthDateTime: LocalDateTime,
        isYajaTime: Boolean,
        progressCallback: suspend (Int) -> Unit
    ): ProcessResult {
        // Get naming engine
        Log.d(TAG, "Getting naming engine instance...")
        val namingEngine = try {
            NamingEngineProvider.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get naming engine", e)
            return ProcessResult(false, "Failed to initialize naming engine: ${e.message}")
        }

        progressCallback(30)

        // Evaluate the name
        Log.d(TAG, "Calling naming engine generateNames...")
        val evaluatedNames = try {
            namingEngine.generateNames(
                userInput = evaluationInput,
                birthDateTime = birthDateTime,
                useYajasi = isYajaTime,
                verbose = true,
                withoutFilter = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Naming engine generateNames failed", e)
            return ProcessResult(false, "Failed to evaluate name: ${e.message}")
        }

        progressCallback(60)

        if (evaluatedNames.isEmpty()) {
            Log.e(TAG, "No evaluation result returned")
            return ProcessResult(false, "No evaluation result")
        }

        val evaluatedName = evaluatedNames.first()
        Log.d(TAG, "First evaluated name: ${evaluatedName.combinedPronounciation}(${evaluatedName.combinedHanja})")

        // Calculate namebom score
        Log.d(TAG, "Calculating namebom score...")
        val namebomScore = try {
            ProfileScoreCalculator.calculateNamebomScore(evaluatedName)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate namebom score", e)
            0
        }
        Log.d(TAG, "Namebom score: $namebomScore")

        progressCallback(80)

        return ProcessResult(
            success = true,
            evaluatedName = evaluatedName,
            namebomScore = namebomScore
        )
    }
}