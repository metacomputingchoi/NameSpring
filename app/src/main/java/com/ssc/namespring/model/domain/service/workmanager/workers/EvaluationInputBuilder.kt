// model/domain/service/workmanager/workers/EvaluationInputBuilder.kt
package com.ssc.namespring.model.domain.service.workmanager.workers

import android.util.Log

internal class EvaluationInputBuilder {

    companion object {
        private const val TAG = "EvaluationInputBuilder"
    }

    fun buildEvaluationInput(
        surnameKorean: String,
        surnameHanja: String,
        givenNameKorean: String,
        givenNameHanja: String
    ): String? {
        return try {
            val evaluationInput = "[$surnameKorean/$surnameHanja]" +
                    givenNameKorean.toCharArray()
                        .zip(givenNameHanja.toCharArray())
                        .joinToString("") { (k, h) -> "[$k/$h]" }

            Log.d(TAG, "Built evaluation input: $evaluationInput")
            evaluationInput
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build evaluation input", e)
            null
        }
    }
}