// model/application/service/AnalysisService.kt
package com.ssc.namespring.model.application.service

import android.content.Context
import com.ssc.namespring.model.application.facade.NameAnalyzer
import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.domain.name.builder.NameAnalysisRequestBuilder
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.NameScore

class AnalysisService(context: Context) {
    private val nameAnalyzer = NameAnalyzer(context)

    fun analyzeNames(
        surHangul: String?,
        surHanja: String,
        name1Hangul: String?,
        name1Hanja: String?,
        name2Hangul: String?,
        name2Hanja: String?,
        birthYear: Int,
        birthMonth: Int,
        birthDay: Int,
        birthHour: Int,
        birthMinute: Int,
        targetName: String? = null
    ): AnalysisResult {
        val fourJu = nameAnalyzer.get4Ju(birthYear, birthMonth, birthDay, birthHour, birthMinute)
        val dictElementsCount = nameAnalyzer.getDictElementsCount(fourJu)

        val builder = NameAnalysisRequestBuilder()
            .withSurname(surHangul, surHanja)
            .withFirstName(name1Hangul, name1Hanja)
            .withSecondName(name2Hangul, name2Hanja)
            .withBirthInfo(birthYear, birthMonth, birthDay, birthHour, birthMinute)
            .withElementBalance(dictElementsCount)

        val analysisResult = nameAnalyzer.analyzeNamesWithBuilder(builder)

        var targetFound = false
        var targetNameInfo: Name? = null
        var targetScore: NameScore? = null

        if (!targetName.isNullOrEmpty()) {
            analysisResult.names.find { it.combinedHanja == targetName }?.let { name ->
                targetFound = true
                targetNameInfo = name
                targetScore = nameAnalyzer.calculateDetailedScore(name)
            }
        }

        return AnalysisResult(
            names = analysisResult.names,
            totalNames = analysisResult.names.size,
            targetName = targetName,
            targetFound = targetFound,
            targetNameInfo = targetNameInfo,
            targetScore = targetScore,
            fourJu = fourJu,
            elementBalance = dictElementsCount
        )
    }

    fun calculateDetailedScore(name: Name): NameScore {
        return nameAnalyzer.calculateDetailedScore(name)
    }

    fun getSaju(year: Int, month: Int, day: Int, hour: Int, minute: Int) =
        nameAnalyzer.get4Ju(year, month, day, hour, minute)

    fun getElementBalance(saju: com.ssc.namespring.model.domain.saju.entity.Saju) =
        nameAnalyzer.getDictElementsCount(saju)
}