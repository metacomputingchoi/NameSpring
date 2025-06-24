// presentation/presenter/AnalysisPresenter.kt
package com.ssc.namespring.presentation.presenter

import com.ssc.namespring.model.application.service.AnalysisService
import com.ssc.namespring.model.application.service.report.ReportService
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.presentation.formatter.ReportFormatter
import com.ssc.namespring.presentation.formatter.SimpleReportFormatter

class AnalysisPresenter(
    private val analysisService: AnalysisService,
    private val reportService: ReportService,
    private val formatter: ReportFormatter = SimpleReportFormatter()
) {
    fun presentBasicNameReport(name: Name) {
        val scores = analysisService.calculateDetailedScore(name)
        val (basicReport, _) = reportService.generateBasicReport(name)

        val output = formatter.formatBasicReport(name, basicReport, scores)
        println(output)
    }

    fun presentDetailedNameReport(name: Name) {
        val scores = analysisService.calculateDetailedScore(name)
        val (basicReport, _) = reportService.generateBasicReport(name)
        val (detailedReport, _) = reportService.generateDetailedReport(name)

        val basicOutput = formatter.formatBasicReport(name, basicReport, scores)
        println(basicOutput)
        println("\n\n")

        val detailedOutput = formatter.formatDetailedReport(name, detailedReport, scores)
        println(detailedOutput)
    }

    fun presentNameList(names: List<Name>, topCount: Int = 5) {
        println("\n=== 생성된 이름 목록 (상위 ${topCount}개) ===")
        names.take(topCount).forEachIndexed { index, name ->
            val score = analysisService.calculateDetailedScore(name)
            println("${index + 1}. ${name.surHanja}${name.combinedHanja} (${name.surHangul}${name.combinedPronounciation}) - 총점: ${score.total}점")
        }
        println("총 ${names.size}개의 이름이 생성되었습니다.")
    }
}