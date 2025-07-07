// model/domain/service/report/generators/EvaluationSectionGenerator.kt
package com.ssc.namespring.model.domain.service.report.generators

import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.report.interfaces.IReportSectionGenerator
import java.text.SimpleDateFormat
import java.util.*

class EvaluationSectionGenerator : IReportSectionGenerator {

    override fun getSectionName(): String = "nameEvaluation"

    override fun generateSection(profile: Profile): Map<String, Any> {
        return mapOf(
            "totalScore" to profile.nameBomScore,
            "scoreTheme" to profile.getScoreThemeColor().name,
            "scoreThemeDescription" to getThemeDescription(profile),
            "scoreCategory" to getScoreCategory(profile.nameBomScore),
            "evaluationDate" to getEvaluationDate(profile),
            "givenName" to createGivenNameInfo(profile)
        )
    }

    private fun getThemeDescription(profile: Profile): String {
        return when (profile.getScoreThemeColor()) {
            Profile.ScoreTheme.SUNNY_SPRING -> "화창한 봄"
            Profile.ScoreTheme.WARM_SPRING -> "따뜻한 봄"
            Profile.ScoreTheme.CLOUDY_SPRING -> "구름 낀 봄"
            Profile.ScoreTheme.RAINY_SPRING -> "비 오는 봄"
            Profile.ScoreTheme.COLD_SPRING -> "추운 봄"
            Profile.ScoreTheme.NOT_EVALUATED -> "미평가"
        }
    }

    private fun getScoreCategory(score: Int): String {
        return when (score) {
            in 90..100 -> "매우 우수"
            in 80..89 -> "우수"
            in 70..79 -> "양호"
            in 60..69 -> "보통"
            else -> "미흡"
        }
    }

    private fun getEvaluationDate(profile: Profile): String {
        return if (profile.nameBomScore > 0) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(profile.updatedAt))
        } else "미평가"
    }

    private fun createGivenNameInfo(profile: Profile): Map<String, Any> {
        return mapOf(
            "fullName" to profile.getFullName(),
            "fullNameWithHanja" to profile.getFullNameWithHanja(),
            "characterCount" to profile.nameCharCount
        )
    }
}