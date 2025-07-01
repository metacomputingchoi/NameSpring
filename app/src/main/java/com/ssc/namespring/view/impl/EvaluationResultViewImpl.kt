// view/impl/EvaluationResultViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.model.data.EvaluationReport
import com.ssc.namespring.model.data.ScoreLevel
import com.ssc.namespring.utils.JsonLoader
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

        val grade = JsonLoader.getGrade(score)
        val gradeAssessment = JsonLoader.scoreEvaluations.gradeAssessments[grade]

        logger.d("=== 종합 평가 결과 ===")
        logger.d("$sprout 이름봄 점수: ${score}점 (${grade}등급)")
        logger.d(gradeAssessment ?: "")
    }

    override fun showRadarChart(scores: Map<String, Int>) {
        logger.d("")
        logger.d("=== 평가 항목별 점수 (100점 만점) ===")
        logger.d("")

        // 텍스트 기반 막대 그래프로 시각화
        scores.forEach { (category, score) ->
            val bar = createVisualBar(score)
            logger.d("$category: $bar ${score}점")
        }

        logger.d("")
        logger.d("=== 레이더 차트 (텍스트 표현) ===")
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

        // 강점과 약점 요약
        val (strengths, weaknesses) = report.getStrengthsAndWeaknesses()
        if (strengths.isNotEmpty() || weaknesses.isNotEmpty()) {
            logger.d("")
            logger.d("【강점과 약점 요약】")
            if (strengths.isNotEmpty()) {
                logger.d("✨ 강점: ${strengths.joinToString(", ")}")
            }
            if (weaknesses.isNotEmpty()) {
                logger.d("⚠️ 약점: ${weaknesses.joinToString(", ")}")
            }
        }

        // 성격 분석 표시
        report.personalityAnalysis?.let { personality ->
            logger.d("")
            logger.d("=== 성격 분석 ===")
            logger.d("【핵심 성격】")
            logger.d(personality.coreTraits.joinToString(", "))
            logger.d("")
            logger.d("【상세 설명】")
            logger.d(personality.description)
            logger.d("")
            logger.d("【강점】")
            personality.strengths.forEach { strength ->
                logger.d("✅ $strength")
            }
            logger.d("")
            logger.d("【주의사항】")
            personality.weaknesses.forEach { weakness ->
                logger.d("⚠️ $weakness")
            }
        }

        // 적합 직업 표시
        report.careerGuidance?.let { career ->
            logger.d("")
            logger.d("=== 적합 직업 및 진로 ===")
            logger.d("【추천 직업】")
            logger.d(career.recommendedCareers.joinToString(", "))
            logger.d("")
            logger.d("【추천 분야】")
            career.careerFields.forEach { field ->
                logger.d("• $field")
            }
            logger.d("")
            logger.d("【업무 스타일】")
            logger.d(career.workStyle)
            logger.d("")
            logger.d("【성공 요인】")
            career.successFactors.forEach { factor ->
                logger.d("💡 $factor")
            }
        }

        // 인생 시기별 분석 표시
        report.lifePeriodAnalysis?.let { lifePeriod ->
            logger.d("")
            logger.d("=== 인생 시기별 운세 ===")
            logger.d("【전체 흐름】")
            logger.d(lifePeriod.overallFlow)
            logger.d("")

            lifePeriod.periods.forEach { period ->
                logger.d("【${period.name}】")
                logger.d(period.description)
                logger.d("📌 ${period.advice}")

                if (period.challenges.isNotEmpty()) {
                    logger.d("⚠️ 주의: ${period.challenges.joinToString(", ")}")
                }
                if (period.opportunities.isNotEmpty()) {
                    logger.d("🌟 기회: ${period.opportunities.joinToString(", ")}")
                }
                logger.d("")
            }

            if (lifePeriod.criticalAges.isNotEmpty()) {
                logger.d("【주요 변화 시기】")
                logger.d("${lifePeriod.criticalAges.joinToString(", ")}세")
            }
        }
    }

    private fun showScoreDetail(title: String, detail: com.ssc.namespring.model.data.ScoreDetail) {
        val levelIcon = when (detail.level) {
            ScoreLevel.EXCELLENT -> "⭐⭐⭐⭐⭐"
            ScoreLevel.GOOD -> "⭐⭐⭐⭐"
            ScoreLevel.AVERAGE -> "⭐⭐⭐"
            ScoreLevel.BELOW -> "⭐⭐"
            ScoreLevel.POOR -> "⭐"
        }

        val levelText = when (detail.level) {
            ScoreLevel.EXCELLENT -> "매우 우수"
            ScoreLevel.GOOD -> "우수"
            ScoreLevel.AVERAGE -> "보통"
            ScoreLevel.BELOW -> "미흡"
            ScoreLevel.POOR -> "개선 필요"
        }

        // 프로그레스 바 생성
        val progressBar = createProgressBar(detail.score)

        logger.d("")
        logger.d("【$title】 ${detail.score}점 $levelIcon ($levelText)")
        logger.d("  $progressBar")
        logger.d("  ${detail.description}")

        // 상세 분석 표시
        detail.analysis.split("\n").forEach { line ->
            if (line.isNotEmpty()) {
                logger.d("  $line")
            }
        }
    }

    private fun createProgressBar(score: Int): String {
        val settings = JsonLoader.formatSettings.progressBar
        val filled = (score * settings.barLength / 100).coerceIn(0, settings.barLength)
        val empty = settings.barLength - filled

        return buildString {
            append("[")
            repeat(filled) { append(settings.filledBar) }
            repeat(empty) { append(settings.emptyBar) }
            append("] ${score}%")
        }
    }

    private fun createVisualBar(score: Int): String {
        val barLength = 20
        val filled = (score * barLength / 100).coerceIn(0, barLength)
        val empty = barLength - filled

        return buildString {
            append("[")
            repeat(filled) { append("█") }
            repeat(empty) { append("░") }
            append("]")
        }
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

        // 점수별 평가 메시지
        val evaluation = when {
            score >= 80 -> JsonLoader.scoreEvaluations.overallEvaluations["80_above"]
            score >= 70 -> JsonLoader.scoreEvaluations.overallEvaluations["70_above"]
            score >= 60 -> JsonLoader.scoreEvaluations.overallEvaluations["60_above"]
            else -> JsonLoader.scoreEvaluations.overallEvaluations["60_below"]
        }

        logger.d("테마: $theme")
        logger.d(evaluation ?: "")
    }

    override fun showError(message: String) {
        logger.e("오류: $message")
    }
}