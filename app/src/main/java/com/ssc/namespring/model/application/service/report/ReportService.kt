// model/application/service/report/ReportService.kt
package com.ssc.namespring.model.application.service.report

import android.content.Context
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.NameScore
import com.ssc.namespring.model.domain.name.value.NameReport
import com.ssc.namespring.model.domain.name.value.DetailedNameReport
import com.ssc.namespring.model.application.service.ScoreCalculationService
import com.ssc.namespring.model.application.service.report.builder.ReportBuilder
import com.ssc.namespring.model.application.service.report.builder.DetailedReportBuilder
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class ReportService(
    context: Context,
    private val scoreCalculationService: ScoreCalculationService
) {
    init {
        ReportDataHolder.initialize(context)
    }

    private val reportBuilder = ReportBuilder()
    private val detailedReportBuilder = DetailedReportBuilder()

    fun generateBasicReport(result: Name): Pair<NameReport, NameScore> {
        val scores = scoreCalculationService.calculateDetailedScore(result)
        val report = reportBuilder.build(result, scores)
        return report to scores
    }

    fun generateDetailedReport(result: Name): Pair<DetailedNameReport, NameScore> {
        val scores = scoreCalculationService.calculateDetailedScore(result)
        val report = detailedReportBuilder.build(result, scores)
        return report to scores
    }
}