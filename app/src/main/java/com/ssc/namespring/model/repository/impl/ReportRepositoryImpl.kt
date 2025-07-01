// model/repository/impl/ReportRepositoryImpl.kt
package com.ssc.namespring.model.repository.impl

import com.ssc.namespring.model.data.ComparisonReport
import com.ssc.namespring.model.data.EvaluationReport
import com.ssc.namespring.model.repository.ReportRepository
import kotlinx.coroutines.delay

/**
 * 메모리 기반 보고서 저장소 구현
 * 실제 앱에서는 Room Database로 교체
 */
class ReportRepositoryImpl : ReportRepository {

    private val evaluationReports = mutableMapOf<String, EvaluationReport>()
    private val comparisonReports = mutableMapOf<String, ComparisonReport>()

    override suspend fun saveEvaluationReport(report: EvaluationReport) {
        delay(10)
        evaluationReports[report.id] = report
    }

    override suspend fun saveComparisonReport(report: ComparisonReport) {
        delay(10)
        comparisonReports[report.id] = report
    }

    override suspend fun getEvaluationReport(id: String): EvaluationReport? {
        delay(5)
        return evaluationReports[id]
    }

    override suspend fun getComparisonReport(id: String): ComparisonReport? {
        delay(5)
        return comparisonReports[id]
    }

    override suspend fun getAllEvaluationReports(): List<EvaluationReport> {
        delay(5)
        return evaluationReports.values.toList()
    }

    override suspend fun getAllComparisonReports(): List<ComparisonReport> {
        delay(5)
        return comparisonReports.values.toList()
    }

    override suspend fun getEvaluationReportsByProfile(profileId: String): List<EvaluationReport> {
        delay(5)
        return evaluationReports.values.filter { it.profile.id == profileId }
    }

    override suspend fun getComparisonReportsByProfile(profileId: String): List<ComparisonReport> {
        delay(5)
        return comparisonReports.values.filter { it.profile.id == profileId }
    }

    override suspend fun deleteEvaluationReport(id: String) {
        delay(10)
        evaluationReports.remove(id)
    }

    override suspend fun deleteComparisonReport(id: String) {
        delay(10)
        comparisonReports.remove(id)
    }

    override suspend fun getRecentReports(limit: Int): List<Any> {
        delay(5)
        val allReports = mutableListOf<Any>()
        allReports.addAll(evaluationReports.values)
        allReports.addAll(comparisonReports.values)

        return allReports
            .sortedByDescending { report ->
                when (report) {
                    is EvaluationReport -> report.createdAt
                    is ComparisonReport -> report.createdAt
                    else -> null
                }
            }
            .take(limit)
    }
}