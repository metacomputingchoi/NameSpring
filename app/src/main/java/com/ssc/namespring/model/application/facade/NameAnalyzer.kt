// model/application/facade/NameAnalyzer.kt
package com.ssc.namespring.model.application.facade

import android.content.Context
import com.ssc.namespring.model.application.di.ServiceModule
import com.ssc.namespring.model.domain.name.builder.NameAnalysisRequestBuilder
import com.ssc.namespring.model.domain.name.value.NameAnalysisResult
import com.ssc.namespring.model.domain.saju.entity.Saju
import com.ssc.namespring.model.domain.element.entity.ElementBalance
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.NameScore

class NameAnalyzer(context: Context) {

    private val serviceContainer = ServiceModule.provideServiceContainer(context)
    private val analysisService = serviceContainer.standardNameAnalysisService
    private val scoreCalculationService = serviceContainer.scoreCalculationService
    private val sajuService = serviceContainer.sajuService

    fun get4Ju(year: Int, month: Int, day: Int, hour: Int, minute: Int): Saju {
        return sajuService.calculateSaju(year, month, day, hour, minute)
    }

    fun getDictElementsCount(fourJu: Saju): ElementBalance {
        return sajuService.calculateElementBalance(fourJu)
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
}