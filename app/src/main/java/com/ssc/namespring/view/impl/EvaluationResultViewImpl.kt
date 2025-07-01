// view/impl/EvaluationResultViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.model.data.EvaluationReport
import com.ssc.namespring.model.data.ScoreLevel
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.EvaluationResultView

class EvaluationResultViewImpl(private val activity: Activity) : EvaluationResultView {

    private val logger = AndroidLogger("EvaluationResultView")

    override fun showOverallScore(score: Int) {
        val sprout = when (score) {
            in 80..100 -> "🌸"
            in 60..79 -> "🌳"
            in 40..59 -> "🌿"
            in 20..39 -> "🌱"
            else -> "🌰"
        }

        logger.d("=== 종합 평가 결과 ===")
        logger.d("$sprout 이름봄 점수: ${score}점")
    }

    override fun showRadarChart(scores: Map<String, Int>) {
        logger.d("")
        logger.d("=== 평가 항목별 점수 ===")
        logger.d("      사주보완")
        logger.d("         ${scores["사주보완"] ?: 0}")
        logger.d("    /    |    \\")
        logger.d("음양균형--+--오행조화")
        logger.d(" ${scores["음양균형"] ?: 0}     |     ${scores["오행조화"] ?: 0}")
        logger.d("   \\    |    /")
        logger.d("  획수길흉  발음자연")
        logger.d("    ${scores["획수길흉"] ?: 0}    ${scores["발음자연"] ?: 0}")
    }

    override fun showDetailedAnalysis(report: EvaluationReport) {
        logger.d("")
        logger.d("=== 상세 분석 ===")

        // 사주 보완도
        showScoreDetail("사주 보완도", report.sajuCompensation)

        // 음양 균형도
        showScoreDetail("음양 균형도", report.yinYangBalance)

        // 오행 조화도
        showScoreDetail("오행 조화도", report.fiveElementsHarmony)

        // 획수 길흉
        showScoreDetail("획수 길흉", report.strokeAuspiciousness)

        // 발음 자연스러움
        showScoreDetail("발음 자연스러움", report.pronunciationNaturalness)
    }

    private fun showScoreDetail(title: String, detail: com.ssc.namespring.model.data.ScoreDetail) {
        val levelIcon = when (detail.level) {
            ScoreLevel.EXCELLENT -> "⭐⭐⭐⭐⭐"
            ScoreLevel.GOOD -> "⭐⭐⭐⭐"
            ScoreLevel.AVERAGE -> "⭐⭐⭐"
            ScoreLevel.BELOW -> "⭐⭐"
            ScoreLevel.POOR -> "⭐"
        }

        logger.d("")
        logger.d("【$title】 ${detail.score}점 $levelIcon")
        logger.d("  ${detail.description}")
        logger.d("  ${detail.analysis}")
    }

    override fun showRecommendations(recommendations: List<String>) {
        if (recommendations.isNotEmpty()) {
            logger.d("")
            logger.d("=== 추천사항 ===")
            recommendations.forEach { rec ->
                logger.d("✅ $rec")
            }
        }
    }

    override fun showImprovements(improvements: List<String>) {
        if (improvements.isNotEmpty()) {
            logger.d("")
            logger.d("=== 개선사항 ===")
            improvements.forEach { imp ->
                logger.d("⚠️ $imp")
            }
        }
    }

    override fun showSaveOptions() {
        logger.d("")
        logger.d("[PDF 저장] [즐겨찾기 추가]")
    }

    override fun showShareOptions() {
        logger.d("[공유하기]")
    }

    override fun applyScoreTheme(score: Int) {
        val theme = when (score) {
            in 80..100 -> "화창한 봄 🌸"
            in 60..79 -> "따뜻한 봄 🌷"
            in 40..59 -> "흐린 봄 🌫️"
            in 20..39 -> "비내리는 봄 🌧️"
            else -> "쌀쌀한 봄 ❄️"
        }
        logger.d("테마: $theme")
    }

    override fun showError(message: String) {
        logger.e("오류: $message")
    }
}