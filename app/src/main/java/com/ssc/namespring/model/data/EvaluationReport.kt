// model/data/EvaluationReport.kt
package com.ssc.namespring.model.data

import com.ssc.namingengine.data.GeneratedName
import java.time.LocalDateTime

data class EvaluationReport(
    val id: String,
    val evaluatedName: GeneratedName,
    val profile: Profile,
    val overallScore: Int,                    // 종합 점수 (0-100)
    val sajuCompensation: ScoreDetail,        // 사주 보완도
    val yinYangBalance: ScoreDetail,          // 음양 균형도
    val fiveElementsHarmony: ScoreDetail,     // 오행 조화도
    val strokeAuspiciousness: ScoreDetail,    // 획수 길흉
    val pronunciationNaturalness: ScoreDetail, // 발음 자연스러움
    val recommendations: List<String>,         // 추천사항
    val improvements: List<String>,            // 개선사항
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun getRadarChartData(): Map<String, Int> = mapOf(
        "사주보완" to sajuCompensation.score,
        "음양균형" to yinYangBalance.score,
        "오행조화" to fiveElementsHarmony.score,
        "획수길흉" to strokeAuspiciousness.score,
        "발음자연" to pronunciationNaturalness.score
    )

    fun getDisplayName(): String = 
        "${evaluatedName.surnameHangul}${evaluatedName.combinedPronounciation}"

    fun getDisplayHanja(): String = 
        "${evaluatedName.surnameHanja}${evaluatedName.combinedHanja}"

    fun getStrengthsAndWeaknesses(): Pair<List<String>, List<String>> {
        val strengths = mutableListOf<String>()
        val weaknesses = mutableListOf<String>()

        listOf(
            "사주보완" to sajuCompensation,
            "음양균형" to yinYangBalance,
            "오행조화" to fiveElementsHarmony,
            "획수길흉" to strokeAuspiciousness,
            "발음자연" to pronunciationNaturalness
        ).forEach { (name, detail) ->
            when (detail.level) {
                ScoreLevel.EXCELLENT, ScoreLevel.GOOD -> strengths.add(name)
                ScoreLevel.BELOW, ScoreLevel.POOR -> weaknesses.add(name)
                else -> {} // AVERAGE는 제외
            }
        }

        return strengths to weaknesses
    }
}

data class ScoreDetail(
    val score: Int,           // 점수 (0-100)
    val description: String,  // 설명
    val analysis: String,     // 상세 분석
    val level: ScoreLevel     // 등급
) {
    companion object {
        fun fromScore(score: Int, description: String, analysis: String): ScoreDetail {
            val level = when {
                score >= 80 -> ScoreLevel.EXCELLENT
                score >= 60 -> ScoreLevel.GOOD
                score >= 40 -> ScoreLevel.AVERAGE
                score >= 20 -> ScoreLevel.BELOW
                else -> ScoreLevel.POOR
            }
            return ScoreDetail(score, description, analysis, level)
        }
    }
}

enum class ScoreLevel {
    EXCELLENT,  // 매우좋음 (80-100)
    GOOD,       // 좋음 (60-79)
    AVERAGE,    // 보통 (40-59)
    BELOW,      // 미흡 (20-39)
    POOR        // 나쁨 (0-19)
}