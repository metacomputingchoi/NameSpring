// model/domain/service/workmanager/workers/naming/NamingResultBuilder.kt
package com.ssc.namespring.model.domain.service.workmanager.workers.naming

import com.google.gson.Gson
import com.ssc.namespring.model.domain.service.evaluation.ProfileScoreCalculator
import com.ssc.namingengine.data.GeneratedName

/**
 * 작업 결과 구성 담당
 */
internal class NamingResultBuilder {

    private val gson = Gson()

    fun buildResult(
        generatedNames: List<GeneratedName>,
        parsedInput: NamingInputParser.ParsedInput
    ): Map<String, Any> {
        val topNames = generatedNames.take(10)
        val nameSuggestions = topNames.map { buildNameSuggestion(it) }

        return mapOf(
            "suggestions" to nameSuggestions,
            "totalCount" to generatedNames.size,
            "topCount" to topNames.size,
            "timestamp" to System.currentTimeMillis(),
            "parameters" to buildParameters(parsedInput)
        )
    }

    fun serializeRawData(generatedNames: List<GeneratedName>): String {
        return gson.toJson(generatedNames)
    }

    private fun buildNameSuggestion(generatedName: GeneratedName): Map<String, Any> {
        val score = ProfileScoreCalculator.calculateNamebomScore(generatedName)
        val givenNameHanja = generatedName.combinedHanja.substring(
            generatedName.surnameHanja.length
        )
        val givenNamePronunciation = generatedName.combinedPronounciation.substring(
            generatedName.surnameHangul.length
        )

        return mapOf(
            "korean" to givenNamePronunciation,
            "hanja" to givenNameHanja,
            "fullKorean" to generatedName.combinedPronounciation,
            "fullHanja" to generatedName.combinedHanja,
            "meaning" to buildMeaningString(generatedName),
            "score" to score,
            "totalScore" to (generatedName.analysisInfo?.totalScore ?: 0)
        )
    }

    private fun buildMeaningString(generatedName: GeneratedName): String {
        return generatedName.hanjaDetails
            .drop(1) // Skip surname
            .mapNotNull { it.inmyongMeaning.takeIf { meaning -> meaning.isNotBlank() } }
            .joinToString(", ")
            .ifEmpty { "의미 정보 없음" }
    }

    private fun buildParameters(parsedInput: NamingInputParser.ParsedInput): Map<String, Any> {
        return mapOf(
            "surname" to parsedInput.surnameInput,
            "fullInput" to parsedInput.fullInput,
            "birthDateTime" to parsedInput.birthDateTime.toString(),
            "isYajaTime" to parsedInput.isYajaTime
        )
    }
}