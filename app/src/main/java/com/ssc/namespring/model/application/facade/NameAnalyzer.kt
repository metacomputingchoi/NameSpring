// model/application/facade/NameAnalyzer.kt
package com.ssc.namespring.model.application.facade

import android.content.Context
import com.ssc.namespring.model.application.di.ServiceModule
import com.ssc.namespring.model.common.constants.NameAnalyzerConstants
import com.ssc.namespring.model.data.AnalysisResult
import com.ssc.namespring.model.domain.name.builder.NameAnalysisRequestBuilder
import com.ssc.namespring.model.domain.name.value.NameAnalysisResult
import com.ssc.namespring.model.domain.saju.entity.Saju
import com.ssc.namespring.model.domain.element.entity.ElementBalance
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.NameScore

class NameAnalyzer(context: Context) {

    private val serviceContainer = ServiceModule.provideServiceContainer(context)
    private val analysisService = serviceContainer.standardNameAnalysisService
    private val reportService = serviceContainer.reportService
    private val scoreCalculationService = serviceContainer.scoreCalculationService
    private val sajuService = serviceContainer.sajuService

    fun get4Ju(year: Int, month: Int, day: Int, hour: Int, minute: Int): Saju {
        return sajuService.calculateSaju(year, month, day, hour, minute)
    }

    fun getDictElementsCount(fourJu: Saju): ElementBalance {
        return sajuService.calculateElementBalance(fourJu)
    }

    fun findNamesGeneralized(
        surHangul: String?,
        surHanja: String,
        name1Hangul: String? = null,
        name1Hanja: String? = null,
        name2Hangul: String? = null,
        name2Hanja: String? = null,
        dictElementsCount: ElementBalance? = null,
        birthYear: Int = NameAnalyzerConstants.DEFAULT_BIRTH_YEAR,
        birthMonth: Int = NameAnalyzerConstants.DEFAULT_BIRTH_MONTH,
        birthDay: Int = NameAnalyzerConstants.DEFAULT_BIRTH_DAY,
        birthHour: Int = NameAnalyzerConstants.DEFAULT_BIRTH_HOUR,
        birthMinute: Int = NameAnalyzerConstants.DEFAULT_BIRTH_MINUTE
    ): List<Name> {
        val request = NameAnalysisRequestBuilder()
            .withSurname(surHangul, surHanja)
            .withFirstName(name1Hangul, name1Hanja)
            .withSecondName(name2Hangul, name2Hanja)
            .withBirthInfo(birthYear, birthMonth, birthDay, birthHour, birthMinute)
            .withElementBalance(dictElementsCount)
            .build()

        return analysisService.analyzeNames(request)
    }

    fun analyzeNamesWithBuilder(block: NameAnalysisRequestBuilder.() -> Unit): NameAnalysisResult {
        val request = NameAnalysisRequestBuilder().apply(block).build()
        val names = analysisService.analyzeNames(request)

        return NameAnalysisResult(
            names = names,
            saju = request.saju,
            elementBalance = request.elementBalance,
            request = request
        )
    }

    fun calculateDetailedScore(result: Name): NameScore {
        return scoreCalculationService.calculateDetailedScore(result)
    }

    fun printDetailedNameReport(result: Name) {
        reportService.printBasicReport(result)
        println("\n\n")
        reportService.printDetailedReport(result)
    }

    fun runAnalysisWithBuilder(): AnalysisResult {
        val targetName = "克訉"

        val fourJu = get4Ju(
            NameAnalyzerConstants.DEFAULT_BIRTH_YEAR,
            NameAnalyzerConstants.DEFAULT_BIRTH_MONTH,
            NameAnalyzerConstants.DEFAULT_BIRTH_DAY,
            NameAnalyzerConstants.DEFAULT_BIRTH_HOUR,
            NameAnalyzerConstants.DEFAULT_BIRTH_MINUTE
        )
        val dictElementsCount = getDictElementsCount(fourJu)

        val result = analyzeNamesWithBuilder {
            withSurname("김", "金")
            withFirstName(null, "克")
            withSecondName(null, "訉")
            withBirthInfo(
                NameAnalyzerConstants.DEFAULT_BIRTH_YEAR,
                NameAnalyzerConstants.DEFAULT_BIRTH_MONTH,
                NameAnalyzerConstants.DEFAULT_BIRTH_DAY,
                NameAnalyzerConstants.DEFAULT_BIRTH_HOUR,
                NameAnalyzerConstants.DEFAULT_BIRTH_MINUTE
            )
            withElementBalance(dictElementsCount)
        }

        var targetFound = false
        var targetScore: NameScore? = null

        for (name in result.names) {
            if (name.combinedHanja == targetName) {
                targetFound = true
                targetScore = calculateDetailedScore(name)
                break
            }
        }

        return AnalysisResult(
            totalNames = result.names.size,
            targetFound = targetFound,
            targetScore = targetScore,
            fourJu = fourJu,
            elementBalance = dictElementsCount
        )
    }
}