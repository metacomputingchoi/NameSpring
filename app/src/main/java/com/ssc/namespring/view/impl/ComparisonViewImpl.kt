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
        logger.d("=== 비교 결과표 ===")

        val categories = report.getComparisonCategories()
        val nameHeaders = report.comparedNames.map { 
            "${it.surnameHangul}${it.combinedPronounciation}" 
        }

        // 헤더
        logger.d("항목 | ${nameHeaders.joinToString(" | ")}")
        logger.d("-".repeat(50))

        // 각 카테고리별 점수
        categories.forEach { category ->
            val scores = report.getNameScores(category)
            val scoreStr = scores.joinToString(" | ") { "${it.second}점" }
            logger.d("$category | $scoreStr")
        }
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
        }
    }

    override fun showWinner(winner: GeneratedName) {
        logger.d("")
        logger.d("🏆 최종 추천: ${winner.surnameHangul}${winner.combinedPronounciation}")
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
}