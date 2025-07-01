// model/data/ComparisonReport.kt
package com.ssc.namespring.model.data

import com.ssc.namingengine.data.GeneratedName
import java.time.LocalDateTime

data class ComparisonReport(
    val id: String,
    val profile: Profile,
    val comparedNames: List<GeneratedName>,
    val comparisonResults: Map<String, List<ComparisonScore>>, // 항목별 점수 리스트
    val rankings: List<RankingResult>,                        // 순위 결과
    val winnerName: GeneratedName,                            // 최종 추천 이름
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun getComparisonCategories(): List<String> = 
        listOf("사주보완", "음양균형", "오행조화", "획수길흉", "발음자연", "종합점수")

    fun getNameScores(category: String): List<Pair<String, Int>> =
        comparisonResults[category]?.map { score ->
            "${score.name.surnameHangul}${score.name.combinedPronounciation}" to score.score
        } ?: emptyList()

    fun getWinnerFeatures(): List<String> {
        val features = mutableListOf<String>()

        // 1위를 차지한 항목들 추출
        comparisonResults.forEach { (category, scores) ->
            scores.find { it.name == winnerName && it.rank == 1 }?.let {
                features.add("${category} 1위")
            }
        }

        return features
    }
}

data class ComparisonScore(
    val name: GeneratedName,
    val score: Int,
    val rank: Int
)

data class RankingResult(
    val rank: Int,
    val name: GeneratedName,
    val totalScore: Int,
    val strengths: List<String>,    // 강점
    val weaknesses: List<String>    // 약점
) {
    fun getDisplayName(): String = 
        "${name.surnameHangul}${name.combinedPronounciation}"

    fun getDisplayHanja(): String = 
        "${name.surnameHanja}${name.combinedHanja}"
}