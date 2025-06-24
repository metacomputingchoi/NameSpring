// model/application/di/ServiceModule.kt
package com.ssc.namespring.model.application.di

import android.content.Context
import com.ssc.namespring.model.application.service.*
import com.ssc.namespring.model.application.service.impl.StandardNameAnalysisService
import com.ssc.namespring.model.application.service.report.ReportService
import com.ssc.namespring.model.infrastructure.repository.impl.JsonHanjaRepository
import com.ssc.namespring.model.infrastructure.repository.impl.JsonNameRepository
import com.ssc.namespring.model.infrastructure.repository.impl.JsonSajuRepository

object ServiceModule {
    private var serviceContainer: ServiceContainer? = null

    fun provideServiceContainer(context: Context): ServiceContainer {
        return serviceContainer ?: synchronized(this) {
            serviceContainer ?: createServiceContainer(context).also { serviceContainer = it }
        }
    }

    private fun createServiceContainer(context: Context): ServiceContainer {
        val hanjaRepository = JsonHanjaRepository(context)
        val sajuRepository = JsonSajuRepository(context)
        val nameRepository = JsonNameRepository(context)

        val sajuService = SajuService(sajuRepository)
        val nameCombinationService = NameCombinationService(hanjaRepository.getStrokeMap())
        val nameFilteringService = NameFilteringService(hanjaRepository, nameRepository)
        val scoreCalculationService = ScoreCalculationService()
        val reportService = ReportService(context, scoreCalculationService)
        val standardNameAnalysisService = StandardNameAnalysisService(
            sajuService,
            nameCombinationService,
            nameFilteringService,
            nameRepository
        )

        return ServiceContainer(
            sajuService = sajuService,
            nameCombinationService = nameCombinationService,
            nameFilteringService = nameFilteringService,
            scoreCalculationService = scoreCalculationService,
            reportService = reportService,
            standardNameAnalysisService = standardNameAnalysisService,
            hanjaRepository = hanjaRepository,
            sajuRepository = sajuRepository,
            nameRepository = nameRepository
        )
    }
}