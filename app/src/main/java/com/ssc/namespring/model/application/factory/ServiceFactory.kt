// model/application/factory/ServiceFactory.kt
package com.ssc.namespring.model.application.factory

import android.content.Context
import com.ssc.namespring.model.application.service.*
import com.ssc.namespring.model.application.service.report.ReportService
import com.ssc.namespring.model.infrastructure.repository.impl.*

class ServiceFactory(private val context: Context) {

    internal val hanjaRepository by lazy { JsonHanjaRepository(context) }
    internal val sajuRepository by lazy { JsonSajuRepository(context) }
    internal val nameRepository by lazy { JsonNameRepository(context) }

    fun createSajuService(): SajuService {
        return SajuService(sajuRepository)
    }

    fun createNameAnalysisService(): NameAnalysisService {
        return NameAnalysisService(hanjaRepository.getStrokeMap())
    }

    fun createNameFilteringService(): NameFilteringService {
        return NameFilteringService(
            hanjaRepository,
            nameRepository,
            createNameAnalysisService()
        )
    }

    fun createScoreCalculationService(): ScoreCalculationService {
        return ScoreCalculationService()
    }

    fun createReportService(): ReportService {
        return ReportService(
            context,
            createScoreCalculationService()
        )
    }
}