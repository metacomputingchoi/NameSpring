// view/utils/ViewLogger.kt
package com.ssc.namespring.view.utils

import com.ssc.namespring.utils.JsonLoader
import com.ssc.namespring.utils.UiHelper
import com.ssc.namespring.utils.logger.AndroidLogger

/**
 * View 레이어에서 사용할 로깅 유틸리티
 * 더 시각적이고 의미있는 로깅 제공
 */
object ViewLogger {

    private val logger = AndroidLogger("ViewLogger")

    /**
     * 구분선 스타일
     */
    enum class LineStyle {
        THICK,     // ═══════════════
        THIN,      // ───────────────
        DOTTED,    // ···············
        DASHED,    // ━━━━━━━━━━━━━━━
        DOUBLE     // ═══════════════
    }

    /**
     * 섹션 헤더 출력
     */
    fun logSection(title: String, style: LineStyle = LineStyle.DASHED) {
        val line = when (style) {
            LineStyle.THICK -> "═".repeat(50)
            LineStyle.THIN -> "─".repeat(50)
            LineStyle.DOTTED -> "·".repeat(50)
            LineStyle.DASHED -> "━".repeat(50)
            LineStyle.DOUBLE -> "═".repeat(50)
        }

        logger.d("")
        logger.d(line)
        logger.d(centerText(title, 50))
        logger.d(line)
    }

    /**
     * 박스 형태로 메시지 출력
     */
    fun logBox(messages: List<String>, width: Int = 50) {
        logger.d("┌${"─".repeat(width - 2)}┐")
        messages.forEach { message ->
            val padding = (width - 4 - message.length) / 2
            val leftPad = " ".repeat(padding.coerceAtLeast(0))
            val rightPad = " ".repeat((width - 4 - message.length - padding).coerceAtLeast(0))
            logger.d("│ $leftPad$message$rightPad │")
        }
        logger.d("└${"─".repeat(width - 2)}┘")
    }

    /**
     * 프로그레스 표시와 함께 로깅
     */
    fun logWithProgress(message: String, current: Int, total: Int) {
        val progress = UiHelper.createProgressBar(current, total, 20)
        logger.d("$message $progress")
    }

    /**
     * 결과 요약 박스
     */
    fun logResultSummary(title: String, items: List<Pair<String, String>>) {
        val maxKeyLength = items.maxOfOrNull { it.first.length } ?: 0

        logSection(title, LineStyle.DOUBLE)
        items.forEach { (key, value) ->
            val padding = " ".repeat(maxKeyLength - key.length)
            logger.d("  $key$padding : $value")
        }
    }

    /**
     * 순위 표시
     */
    fun logRanking(rankings: List<Pair<String, Int>>) {
        logger.d("")
        logger.d("【 순위 】")
        rankings.forEachIndexed { index, (name, score) ->
            val medal = UiHelper.getRankMedal(index + 1)
            val bar = UiHelper.createMiniProgressBar(score)
            logger.d("$medal $name - $bar ${score}점")
        }
    }

    /**
     * 팁 박스
     */
    fun logTipBox(tips: List<String>) {
        logger.d("")
        logger.d("💡 ┌─── 도움말 ───────────────────────────┐")
        tips.forEach { tip ->
            logger.d("💡 │ • $tip")
        }
        logger.d("💡 └────────────────────────────────────────┘")
    }

    /**
     * 경고 박스
     */
    fun logWarningBox(warnings: List<String>) {
        logger.d("")
        logger.d("⚠️  ┌─── 주의사항 ─────────────────────────┐")
        warnings.forEach { warning ->
            logger.d("⚠️  │ • $warning")
        }
        logger.d("⚠️  └────────────────────────────────────────┘")
    }

    /**
     * 성공 메시지
     */
    fun logSuccess(message: String) {
        logger.d("")
        logger.d("✅ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.d("✅  $message")
        logger.d("✅ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    /**
     * 에러 메시지
     */
    fun logError(message: String, details: List<String> = emptyList()) {
        logger.d("")
        logger.d("❌ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.d("❌  오류: $message")
        details.forEach { detail ->
            logger.d("❌  → $detail")
        }
        logger.d("❌ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    /**
     * 단계별 진행 상황
     */
    fun logStep(stepNumber: Int, totalSteps: Int, description: String) {
        logger.d("")
        logger.d("[$stepNumber/$totalSteps] $description")
        val progress = UiHelper.createProgressBar(stepNumber, totalSteps, 30)
        logger.d(progress)
    }

    /**
     * 특별 축하 메시지
     */
    fun logCelebration(message: String, emoji: String = "🎉") {
        val line = "$emoji ".repeat(25)
        logger.d("")
        logger.d(line)
        logger.d(centerText(message, 50))
        logger.d(line)
    }

    /**
     * 텍스트 가운데 정렬
     */
    private fun centerText(text: String, width: Int): String {
        val padding = (width - text.length) / 2
        return " ".repeat(padding.coerceAtLeast(0)) + text
    }

    /**
     * 카테고리별 점수 시각화
     */
    fun logScoreVisualization(scores: Map<String, Int>) {
        logger.d("")
        logger.d("【 항목별 점수 분석 】")

        scores.forEach { (category, score) ->
            val bar = when (score) {
                in 90..100 -> "█".repeat(10)
                in 80..89 -> "█".repeat(9) + "▒"
                in 70..79 -> "█".repeat(8) + "▒".repeat(2)
                in 60..69 -> "█".repeat(7) + "▒".repeat(3)
                in 50..59 -> "█".repeat(6) + "▒".repeat(4)
                in 40..49 -> "█".repeat(5) + "▒".repeat(5)
                in 30..39 -> "█".repeat(4) + "▒".repeat(6)
                in 20..29 -> "█".repeat(3) + "▒".repeat(7)
                in 10..19 -> "█".repeat(2) + "▒".repeat(8)
                else -> "█" + "▒".repeat(9)
            }

            val level = when (score) {
                in 90..100 -> "최우수"
                in 70..89 -> "우수"
                in 50..69 -> "양호"
                in 30..49 -> "보통"
                else -> "미흡"
            }

            logger.d(String.format("%-8s: %s %3d점 (%s)", category, bar, score, level))
        }
    }
}