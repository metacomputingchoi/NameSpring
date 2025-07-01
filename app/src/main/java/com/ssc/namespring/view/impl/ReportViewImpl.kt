// view/impl/ReportViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.model.data.EvaluationReport
import com.ssc.namespring.model.data.ComparisonReport
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.ReportView
import java.time.format.DateTimeFormatter

class ReportViewImpl(private val activity: Activity) : ReportView {

    private val logger = AndroidLogger("ReportView")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")

    override fun showEvaluationReport(report: EvaluationReport) {
        logger.d("=== 평가 보고서 ===")
        logger.d("보고서 ID: ${report.id}")
        logger.d("이름: ${report.getDisplayName()} (${report.getDisplayHanja()})")
        logger.d("평가 대상: ${report.profile.profileName}")
        logger.d("종합 점수: ${report.overallScore}점")
        logger.d("생성일시: ${report.createdAt.format(dateFormatter)}")
        logger.d("")

        logger.d("【상세 평가】")
        logger.d("- 사주 보완도: ${report.sajuCompensation.score}점")
        logger.d("- 음양 균형도: ${report.yinYangBalance.score}점")
        logger.d("- 오행 조화도: ${report.fiveElementsHarmony.score}점")
        logger.d("- 획수 길흉: ${report.strokeAuspiciousness.score}점")
        logger.d("- 발음 자연스러움: ${report.pronunciationNaturalness.score}점")
    }

    override fun showComparisonReport(report: ComparisonReport) {
        logger.d("=== 비교 보고서 ===")
        logger.d("보고서 ID: ${report.id}")
        logger.d("비교 기준: ${report.profile.profileName}")
        logger.d("비교 대상: ${report.comparedNames.size}개")
        logger.d("생성일시: ${report.createdAt.format(dateFormatter)}")
        logger.d("")

        logger.d("【최종 순위】")
        report.rankings.forEach { ranking ->
            logger.d("${ranking.rank}위: ${ranking.getDisplayName()} (${ranking.totalScore}점)")
        }
        logger.d("")
        logger.d("최종 추천: ${report.winnerName.surnameHangul}${report.winnerName.combinedPronounciation}")
    }

    override fun showReportList(reports: List<Any>) {
        logger.d("=== 보고서 목록 (${reports.size}개) ===")

        reports.forEachIndexed { index, report ->
            when (report) {
                is EvaluationReport -> {
                    logger.d("[${index + 1}] 평가 - ${report.getDisplayName()} (${report.overallScore}점)")
                    logger.d("    ${report.createdAt.format(dateFormatter)}")
                }
                is ComparisonReport -> {
                    logger.d("[${index + 1}] 비교 - ${report.comparedNames.size}개 이름")
                    logger.d("    우승: ${report.winnerName.surnameHangul}${report.winnerName.combinedPronounciation}")
                    logger.d("    ${report.createdAt.format(dateFormatter)}")
                }
            }
        }
    }

    override fun showExportOptions() {
        logger.d("")
        logger.d("[내보내기 옵션]")
        logger.d("📄 [PDF로 저장]")
        logger.d("🖼️ [이미지로 저장]")
        logger.d("📋 [텍스트로 복사]")
        logger.d("📤 [공유하기]")
    }

    override fun showExportSuccess(message: String) {
        logger.d("✅ $message")
    }

    override fun showShareSuccess() {
        logger.d("✅ 보고서가 공유되었습니다")
    }

    override fun showDeleteSuccess() {
        logger.d("✅ 보고서가 삭제되었습니다")
    }

    override fun showError(message: String) {
        logger.e("❌ 오류: $message")
    }

    override fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            logger.d("📋 보고서 처리 중...")
        }
    }
}