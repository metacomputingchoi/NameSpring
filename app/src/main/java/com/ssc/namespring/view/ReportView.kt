// view/ReportView.kt
package com.ssc.namespring.view

import com.ssc.namespring.model.data.EvaluationReport
import com.ssc.namespring.model.data.ComparisonReport

/**
 * 보고서 화면 View 인터페이스
 * REQ-FUNC-06 구현
 */
interface ReportView {
    fun showEvaluationReport(report: EvaluationReport)
    fun showComparisonReport(report: ComparisonReport)
    fun showReportList(reports: List<Any>)
    fun showExportOptions()
    fun showExportSuccess(message: String)
    fun showShareSuccess()
    fun showDeleteSuccess()
    fun showError(message: String)
    fun showLoading(isLoading: Boolean)
}