// controller/ReportController.kt
package com.ssc.namespring.controller

import android.content.Context
import com.ssc.namespring.model.ReportModel
import com.ssc.namespring.utils.PdfExportUtil
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.ReportView
import kotlinx.coroutines.*

/**
 * 보고서 관리 컨트롤러
 * REQ-FUNC-06 구현
 */
class ReportController(
    private val reportModel: ReportModel,
    private val reportView: ReportView,
    private val context: Context
) {

    private val logger = AndroidLogger("ReportController")

    // 컨트롤러 전용 코루틴 스코프
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // 보고서 보기 완료 콜백
    var onReportsViewed: (() -> Unit)? = null

    /**
     * 평가 보고서 표시 및 내보내기
     */
    suspend fun showEvaluationReport(reportId: String) {
        reportView.showLoading(true)

        try {
            val report = reportModel.getEvaluationReport(reportId)
            if (report != null) {
                reportView.showEvaluationReport(report)
                reportView.showExportOptions()
            } else {
                reportView.showError("보고서를 찾을 수 없습니다")
            }
        } catch (e: Exception) {
            reportView.showError("보고서 로드 실패: ${e.message}")
        } finally {
            reportView.showLoading(false)
        }
    }

    /**
     * 비교 보고서 표시 및 내보내기
     */
    suspend fun showComparisonReport(reportId: String) {
        reportView.showLoading(true)

        try {
            val report = reportModel.getComparisonReport(reportId)
            if (report != null) {
                reportView.showComparisonReport(report)
                reportView.showExportOptions()
            } else {
                reportView.showError("보고서를 찾을 수 없습니다")
            }
        } catch (e: Exception) {
            reportView.showError("보고서 로드 실패: ${e.message}")
        } finally {
            reportView.showLoading(false)
        }
    }

    /**
     * 최근 보고서 목록 표시
     */
    suspend fun showRecentReports(limit: Int = 10) {
        reportView.showLoading(true)

        try {
            val recentReports = reportModel.getRecentReports(limit)
            reportView.showReportList(recentReports)

            // 테스트: 첫 번째 보고서 자동 선택
            if (recentReports.isNotEmpty()) {
                simulateReportSelection(recentReports.first())
            }
        } catch (e: Exception) {
            reportView.showError("보고서 목록 로드 실패: ${e.message}")
        } finally {
            reportView.showLoading(false)
        }
    }

    /**
     * 프로필별 보고서 표시
     */
    suspend fun showReportsByProfile(profileId: String) {
        reportView.showLoading(true)

        try {
            val evaluationReports = reportModel.getEvaluationReportsByProfile(profileId)
            val comparisonReports = reportModel.getComparisonReportsByProfile(profileId)

            val allReports = mutableListOf<Any>()
            allReports.addAll(evaluationReports)
            allReports.addAll(comparisonReports)

            reportView.showReportList(allReports)

            // 테스트: 첫 번째 보고서 자동 선택
            if (allReports.isNotEmpty()) {
                simulateReportSelection(allReports.first())
            }
        } catch (e: Exception) {
            reportView.showError("보고서 목록 로드 실패: ${e.message}")
        } finally {
            reportView.showLoading(false)
        }
    }

    /**
     * 테스트용 보고서 선택 시뮬레이션
     */
    private suspend fun simulateReportSelection(report: Any) {
        logger.d("")
        logger.d("=== 보고서 선택 시뮬레이션 ===")

        when (report) {
            is com.ssc.namespring.model.data.EvaluationReport -> {
                logger.d("평가 보고서 선택: ${report.getDisplayName()}")
                delay(1000)
                showEvaluationReport(report.id)

                // PDF 저장 시뮬레이션
                delay(1000)
                logger.d("→ PDF로 저장 선택")
                exportEvaluationReportToPdf(report.id)
            }
            is com.ssc.namespring.model.data.ComparisonReport -> {
                logger.d("비교 보고서 선택: ${report.comparedNames.size}개 이름")
                delay(1000)
                showComparisonReport(report.id)

                // PDF 저장 시뮬레이션
                delay(1000)
                logger.d("→ PDF로 저장 선택")
                exportComparisonReportToPdf(report.id)
            }
        }

        // 보고서 보기 완료 콜백 호출
        controllerScope.launch {
            delay(2000)
            onReportsViewed?.invoke()
        }
    }

    /**
     * 평가 보고서를 PDF로 내보내기
     */
    suspend fun exportEvaluationReportToPdf(reportId: String) {
        reportView.showLoading(true)

        try {
            val report = reportModel.getEvaluationReport(reportId)
            if (report != null) {
                val result = PdfExportUtil.exportEvaluationReport(context, report)
                result.onSuccess { file ->
                    reportView.showExportSuccess("PDF가 저장되었습니다: ${file.name}")
                    logger.d("PDF 저장 완료: ${file.absolutePath}")
                }.onFailure { error ->
                    reportView.showError("PDF 생성 실패: ${error.message}")
                }
            } else {
                reportView.showError("보고서를 찾을 수 없습니다")
            }
        } catch (e: Exception) {
            reportView.showError("내보내기 실패: ${e.message}")
        } finally {
            reportView.showLoading(false)
        }
    }

    /**
     * 비교 보고서를 PDF로 내보내기
     */
    suspend fun exportComparisonReportToPdf(reportId: String) {
        reportView.showLoading(true)

        try {
            val report = reportModel.getComparisonReport(reportId)
            if (report != null) {
                val result = PdfExportUtil.exportComparisonReport(context, report)
                result.onSuccess { file ->
                    reportView.showExportSuccess("PDF가 저장되었습니다: ${file.name}")
                    logger.d("PDF 저장 완료: ${file.absolutePath}")
                }.onFailure { error ->
                    reportView.showError("PDF 생성 실패: ${error.message}")
                }
            } else {
                reportView.showError("보고서를 찾을 수 없습니다")
            }
        } catch (e: Exception) {
            reportView.showError("내보내기 실패: ${e.message}")
        } finally {
            reportView.showLoading(false)
        }
    }

    /**
     * 보고서 공유
     */
    suspend fun shareReport(reportId: String, isEvaluationReport: Boolean) {
        reportView.showLoading(true)

        try {
            val shareResult = reportModel.shareReport(reportId)
            shareResult.onSuccess {
                reportView.showShareSuccess()
            }.onFailure { error ->
                reportView.showError("공유 실패: ${error.message}")
            }
        } catch (e: Exception) {
            reportView.showError("공유 실패: ${e.message}")
        } finally {
            reportView.showLoading(false)
        }
    }

    /**
     * 보고서 삭제
     */
    suspend fun deleteReport(reportId: String, isEvaluationReport: Boolean) {
        try {
            if (isEvaluationReport) {
                reportModel.deleteEvaluationReport(reportId)
            } else {
                reportModel.deleteComparisonReport(reportId)
            }
            reportView.showDeleteSuccess()

            // 목록 새로고침
            showRecentReports()
        } catch (e: Exception) {
            reportView.showError("삭제 실패: ${e.message}")
        }
    }
}