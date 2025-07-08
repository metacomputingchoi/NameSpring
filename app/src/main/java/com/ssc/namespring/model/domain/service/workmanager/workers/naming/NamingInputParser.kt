// model/domain/service/workmanager/workers/naming/NamingInputParser.kt
package com.ssc.namespring.model.domain.service.workmanager.workers.naming

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 작업 입력 데이터 파싱 담당
 */
internal class NamingInputParser {

    data class ParsedInput(
        val birthDateTime: LocalDateTime,
        val isYajaTime: Boolean,
        val surnameKorean: String,
        val surnameHanja: String,
        val nameInputFormat: String,
        val nameCharCount: Int,
        val surnameInput: String,
        val fullInput: String
    )

    fun parse(inputData: Map<String, Any?>): ParsedInput {
        val birthDateTimeMillis = (inputData["birthDateTime"] as? String)?.toLongOrNull()
            ?: throw IllegalArgumentException("Birth date time is required")

        val birthDateTime = Instant.ofEpochMilli(birthDateTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        val isYajaTime = inputData["isYajaTime"] as? Boolean ?: true
        val surnameData = inputData["surname"] as? Map<*, *>
        val nameInputFormat = inputData["nameInputFormat"] as? String ?: ""
        val nameCharCount = (inputData["nameCharCount"] as? Double)?.toInt() ?: 2

        val surnameKorean = surnameData?.get("korean")?.toString() ?: ""
        val surnameHanja = surnameData?.get("hanja")?.toString() ?: ""

        if (surnameKorean.isEmpty() || surnameHanja.isEmpty()) {
            throw IllegalArgumentException("Surname information is required")
        }

        val surnameInput = "[$surnameKorean/$surnameHanja]"
        val fullInput = buildFullInput(surnameInput, nameInputFormat, nameCharCount)

        return ParsedInput(
            birthDateTime = birthDateTime,
            isYajaTime = isYajaTime,
            surnameKorean = surnameKorean,
            surnameHanja = surnameHanja,
            nameInputFormat = nameInputFormat,
            nameCharCount = nameCharCount,
            surnameInput = surnameInput,
            fullInput = fullInput
        )
    }

    private fun buildFullInput(
        surnameInput: String, 
        nameInputFormat: String, 
        nameCharCount: Int
    ): String {
        return if (nameInputFormat.isNotEmpty()) {
            surnameInput + nameInputFormat
        } else {
            surnameInput + "[_/_]".repeat(nameCharCount)
        }
    }
}