// model/repository/ReportRepository.kt
package com.ssc.namespring.model.repository

import com.ssc.namespring.model.data.ComparisonReport
import com.ssc.namespring.model.data.EvaluationReport

interface ReportRepository {
    suspend fun saveEvaluationReport(report: EvaluationReport)
    suspend fun saveComparisonReport(report: ComparisonReport)
    suspend fun getEvaluationReport(id: String): EvaluationReport?
    suspend fun getComparisonReport(id: String): ComparisonReport?
    suspend fun getAllEvaluationReports(): List<EvaluationReport>
    suspend fun getAllComparisonReports(): List<ComparisonReport>
    suspend fun getEvaluationReportsByProfile(profileId: String): List<EvaluationReport>
    suspend fun getComparisonReportsByProfile(profileId: String): List<ComparisonReport>
    suspend fun deleteEvaluationReport(id: String)
    suspend fun deleteComparisonReport(id: String)
    suspend fun getRecentReports(limit: Int = 10): List<Any>  // EvaluationReport or ComparisonReport
}