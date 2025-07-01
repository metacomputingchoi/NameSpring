// view/impl/ComparisonViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.data.ComparisonReport
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.data.RankingResult
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.ComparisonView

class ComparisonViewImpl(private val activity: Activity) : ComparisonView {

    private val logger = AndroidLogger("ComparisonView")

    override fun showNameSelectors(count: Int) {
        logger.d("=== 이름 비교 (${count}개) ===")
        for (i in 1..count) {
            logger.d("이름 $i: [선택하세요]")
        }
    }

    override fun showProfileSelector(profiles: List<Profile>) {
        logger.d("비교 기준 프로필 선택:")
        profiles.forEachIndexed { index, profile ->
            logger.d("${index + 1}. ${profile.profileName}")
        }
    }

    override fun showComparisonTable(report: ComparisonReport) {
        logger.d("")
        logger.d("=== 비교 결과표 (100점 만점) ===")

        val categories = report.getComparisonCategories()
        val nameHeaders = report.comparedNames.map {
            "${it.surnameHangul}${it.combinedPronounciation}"
        }

        // 헤더
        logger.d("항목 | ${nameHeaders.joinToString(" | ")}")
        logger.d("-".repeat(60))

        // 각 카테고리별 점수
        categories.forEach { category ->
            val scores = report.getNameScores(category)
            val scoreStr = scores.joinToString(" | ") {
                val score = it.second
                val bar = createMiniBar(score)
                "$bar ${score}점"
            }
            logger.d("$category | $scoreStr")
        }

        logger.d("-".repeat(60))

        // 평균 점수 계산
        val averageScores = report.comparedNames.map { name ->
            val sum = categories.dropLast(1).sumOf { category ->  // 종합점수 제외
                report.comparisonResults[category]?.find { it.name == name }?.score ?: 0
            }
            val avg = sum / (categories.size - 1)
            "${name.surnameHangul}${name.combinedPronounciation}" to avg
        }

        logger.d("평균 | ${averageScores.joinToString(" | ") { "${it.second}점" }}")
    }

    override fun showRankings(rankings: List<RankingResult>) {
        logger.d("")
        logger.d("=== 최종 순위 ===")

        rankings.forEach { ranking ->
            val medal = when (ranking.rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "${ranking.rank}위"
            }

            logger.d("")
            logger.d("$medal ${ranking.getDisplayName()} (${ranking.totalScore}점)")

            if (ranking.strengths.isNotEmpty()) {
                logger.d("   강점: ${ranking.strengths.joinToString(", ")}")
            }
            if (ranking.weaknesses.isNotEmpty()) {
                logger.d("   약점: ${ranking.weaknesses.joinToString(", ")}")
            }

            // 한자 표시
            logger.d("   한자: ${ranking.getDisplayHanja()}")
        }
    }

    override fun showWinner(winner: GeneratedName) {
        logger.d("")
        logger.d("🏆 최종 추천: ${winner.surnameHangul}${winner.combinedPronounciation}")

        // 승자의 주요 특징 표시
        winner.analysisInfo?.let { info ->
            val features = mutableListOf<String>()

            if (info.eumYangInfo.isBalanced) {
                features.add("음양균형")
            }
            if (info.ohaengInfo.overallHarmony.contains("조화")) {
                features.add("오행조화")
            }
            if ((info.scoreBreakdown["사격점수"] ?: 0) >= 75) {
                features.add("우수한 사격")
            }

            if (features.isNotEmpty()) {
                logger.d("   주요 특징: ${features.joinToString(", ")}")
            }
        }
    }

    override fun enableCompareButton(enabled: Boolean) {
        if (enabled) {
            logger.d("[비교하기] 버튼 활성화")
        }
    }

    override fun showError(message: String) {
        logger.e("오류: $message")
    }

    override fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            logger.d("비교 분석 중...")
        }
    }

    private fun createMiniBar(score: Int): String {
        val barLength = 10
        val filled = (score * barLength / 100).coerceIn(0, barLength)

        return buildString {
            repeat(filled) { append("▓") }
            repeat(barLength - filled) { append("░") }
        }
    }
}