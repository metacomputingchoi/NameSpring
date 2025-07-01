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
    val personalityAnalysis: PersonalityAnalysis? = null,  // 성격 분석
    val careerGuidance: CareerGuidance? = null,           // 적합 직업
    val lifePeriodAnalysis: LifePeriodAnalysis? = null,   // 인생 시기별 분석
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

// 성격 분석 결과
data class PersonalityAnalysis(
    val coreTraits: List<String>,      // 핵심 성격 특성
    val strengths: List<String>,       // 강점
    val weaknesses: List<String>,      // 약점
    val description: String            // 종합 설명
)

// 적합 직업 안내
data class CareerGuidance(
    val recommendedCareers: List<String>,  // 추천 직업 목록
    val careerFields: List<String>,        // 추천 분야
    val workStyle: String,                 // 업무 스타일
    val successFactors: List<String>       // 성공 요인
)

// 인생 시기별 분석
data class LifePeriodAnalysis(
    val periods: List<LifePeriod>,        // 시기별 분석
    val overallFlow: String,              // 전체 인생 흐름
    val criticalAges: List<Int>           // 주요 변화 시기
)

data class LifePeriod(
    val name: String,                     // 시기 이름 (유년기, 청년기 등)
    val description: String,              // 시기 설명
    val challenges: List<String>,         // 주의사항
    val opportunities: List<String>,      // 기회 요인
    val advice: String                    // 조언
)