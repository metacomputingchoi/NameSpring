// model/domain/service/workmanager/workers/EvaluationWorker.kt
package com.ssc.namespring.model.domain.service.workmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.ssc.namespring.model.domain.service.workmanager.BaseWorker
import com.ssc.namespring.model.domain.service.workmanager.WorkResult

class EvaluationWorker(
    context: Context,
    params: WorkerParameters
) : BaseWorker(context, params) {

    companion object {
        private const val TAG = "EvaluationWorker"
    }

    private val dataParser = EvaluationDataParser()
    private val inputBuilder = EvaluationInputBuilder()
    private val processor = EvaluationProcessor(applicationContext)
    private val resultBuilder = EvaluationResultBuilder(applicationContext)

    override suspend fun performWork(): WorkResult {
        try {
            Log.d(TAG, "Starting evaluation work for profile: $profileId")

            // Parse input data
            val parsedData = dataParser.parseInputData(getInputDataMap())
            if (!parsedData.isValid) {
                return WorkResult(
                    success = false,
                    error = parsedData.errorMessage ?: "Invalid input data"
                )
            }

            updateProgress(10)

            // Build evaluation input
            val evaluationInput = inputBuilder.buildEvaluationInput(
                parsedData.surnameKorean!!,
                parsedData.surnameHanja!!,
                parsedData.givenNameKorean!!,
                parsedData.givenNameHanja!!
            )

            if (evaluationInput.isNullOrEmpty()) {
                return WorkResult(
                    success = false,
                    error = "Failed to build evaluation input"
                )
            }

            updateProgress(20)

            // Process evaluation
            val evaluationResult = processor.processEvaluation(
                evaluationInput,
                parsedData.birthDateTime!!,
                parsedData.isYajaTime
            ) { progress ->
                updateProgress(20 + (progress * 0.5).toInt())
            }

            if (!evaluationResult.success) {
                return WorkResult(
                    success = false,
                    error = evaluationResult.errorMessage ?: "Evaluation failed"
                )
            }

            updateProgress(70)

            // Build final result
            val finalResult = resultBuilder.buildResult(
                evaluationResult.evaluatedName!!,
                evaluationResult.namebomScore
            )

            updateProgress(100)

            Log.d(TAG, "Evaluation completed successfully")
            return WorkResult(
                success = true,
                data = finalResult.data,
                rawData = finalResult.rawDataJson
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