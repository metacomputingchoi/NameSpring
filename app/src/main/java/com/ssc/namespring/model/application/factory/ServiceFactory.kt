// model/application/factory/ServiceFactory.kt
package com.ssc.namespring.model.application.factory

import android.content.Context
import com.ssc.namespring.model.application.service.*
import com.ssc.namespring.model.application.service.report.ReportService
import com.ssc.namespring.model.infrastructure.repository.HanjaRepository
import com.ssc.namespring.model.infrastructure.repository.NameRepository
import com.ssc.namespring.model.infrastructure.repository.SajuRepository
import com.ssc.namespring.model.infrastructure.repository.impl.JsonHanjaRepository
import com.ssc.namespring.model.infrastructure.repository.impl.JsonNameRepository
import com.ssc.namespring.model.infrastructure.repository.impl.JsonSajuRepository

class ServiceFactory(
    private val context: Context,
    private val hanjaRepository: HanjaRepository = JsonHanjaRepository(context),
    private val sajuRepository: SajuRepository = JsonSajuRepository(context),
    private val nameRepository: NameRepository = JsonNameRepository(context)
) {
    fun createSajuService(): SajuService {
        return SajuService(sajuRepository)
    }

    fun createNameCombinationService(): NameCombinationService {
        return NameCombinationService(hanjaRepository.getStrokeMap())
    }

    fun createNameAnalysisService(): NameAnalysisService {
        return NameAnalysisService()
    }

    fun createNameFilteringService(): NameFilteringService {
        return NameFilteringService(
            hanjaRepository,
            nameRepository
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