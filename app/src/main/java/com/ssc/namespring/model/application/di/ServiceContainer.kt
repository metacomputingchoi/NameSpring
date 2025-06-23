// model/application/di/ServiceContainer.kt
package com.ssc.namespring.model.application.di

import com.ssc.namespring.model.application.service.*
import com.ssc.namespring.model.application.service.impl.StandardNameAnalysisService
import com.ssc.namespring.model.application.service.report.ReportService
import com.ssc.namespring.model.infrastructure.repository.HanjaRepository
import com.ssc.namespring.model.infrastructure.repository.NameRepository
import com.ssc.namespring.model.infrastructure.repository.SajuRepository

data class ServiceContainer(
    val sajuService: SajuService,
    val nameCombinationService: NameCombinationService,
    val nameAnalysisService: NameAnalysisService,
    val nameFilteringService: NameFilteringService,
    val scoreCalculationService: ScoreCalculationService,
    val reportService: ReportService,
    val standardNameAnalysisService: StandardNameAnalysisService,
    val hanjaRepository: HanjaRepository,
    val sajuRepository: SajuRepository,
    val nameRepository: NameRepository
)