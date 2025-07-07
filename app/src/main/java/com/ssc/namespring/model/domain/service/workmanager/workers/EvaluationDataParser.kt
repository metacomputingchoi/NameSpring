// model/domain/service/workmanager/workers/EvaluationDataParser.kt
package com.ssc.namespring.model.domain.service.workmanager.workers

import android.util.Log
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

internal class EvaluationDataParser {

    companion object {
        private const val TAG = "EvaluationDataParser"
    }

    data class ParsedData(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val birthDateTime: LocalDateTime? = null,
        val isYajaTime: Boolean = true,
        val surnameKorean: String? = null,
        val surnameHanja: String? = null,
        val givenNameKorean: String? = null,
        val givenNameHanja: String? = null
    )

    fun parseInputData(inputData: Map<String, Any?>): ParsedData {
        Log.d(TAG, "Input data keys: ${inputData.keys}")

        // Parse birth date time
        val birthDateTimeMillis = (inputData["birthDateTime"] as? String)?.toLongOrNull()
        if (birthDateTimeMillis == null) {
            return ParsedData(false, "Birth date time is required")
        }

        val birthDateTime = try {
            Instant.ofEpochMilli(birthDateTimeMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert birth date time", e)
            return ParsedData(false, "Invalid birth date time: ${e.message}")
        }

        // Parse other fields
        val isYajaTime = inputData["isYajaTime"] as? Boolean ?: true
        val surnameData = inputData["surname"] as? Map<*, *>
        val givenNameData = inputData["givenName"] as? Map<*, *>

        val surnameKorean = surnameData?.get("korean")?.toString() ?: ""
        val surnameHanja = surnameData?.get("hanja")?.toString() ?: ""
        val givenNameKorean = givenNameData?.get("korean")?.toString() ?: ""
        val givenNameHanja = givenNameData?.get("hanja")?.toString() ?: ""

        Log.d(TAG, "Extracted name components:")
        Log.d(TAG, "  Surname: $surnameKorean/$surnameHanja")
        Log.d(TAG, "  Given name: $givenNameKorean/$givenNameHanja")

        if (surnameKorean.isEmpty() || surnameHanja.isEmpty() ||
            givenNameKorean.isEmpty() || givenNameHanja.isEmpty()) {
            return ParsedData(
                false,
                "Complete name information is required"
            )
        }

        return ParsedData(
            isValid = true,
            birthDateTime = birthDateTime,
            isYajaTime = isYajaTime,
            surnameKorean = surnameKorean,
            surnameHanja = surnameHanja,
            givenNameKorean = givenNameKorean,
            givenNameHanja = givenNameHanja
        )
    }
}