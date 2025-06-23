// model/application/service/report/analyzer/evaluation/ScoreEvaluator.kt
package com.ssc.namespring.model.application.service.report.analyzer.evaluation

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.NameScore
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class ScoreEvaluator {
    private val scoreData = ReportDataHolder.scoreEvaluationsData
    private val strings = ReportDataHolder.scoreEvaluatorStrings

    fun evaluate(scores: NameScore): String {
        val strengths = analyzeStrengths(scores)
        val weaknesses = analyzeWeaknesses(scores)

        return buildEvaluation(strengths, weaknesses)
    }

    fun getGrade(totalScore: Int): String {
        val grades = strings.grades

        return when {
            totalScore >= grades["A"]!! -> "A"
            totalScore >= grades["B"]!! -> "B"
            totalScore >= grades["C"]!! -> "C"
            else -> strings.defaultGrade
        }
    }

    fun getRecommendations(scores: NameScore): List<String> {
        val recommendations = scoreData.recommendations
        val grades = strings.grades

        return listOf(
            when {
                scores.total >= grades["A"]!! -> recommendations["80_above"] ?: ""
                scores.total >= grades["B"]!! -> recommendations["70_above"] ?: ""
                scores.total >= grades["C"]!! -> recommendations["60_above"] ?: ""
                else -> recommendations["60_below"] ?: ""
            }
        )
    }

    fun getDetailedRecommendations(result: Name, scores: NameScore): List<String> {
        val recommendations = mutableListOf<String>()
        val detailedRecs = scoreData.detailedRecommendations

        // 점수별 권고사항
        if (scores.fourTypesLuck >= (scoreData.scoreThresholds["score_high_threshold_1"] ?: 7)) {
            detailedRecs["high_four_types"]?.let { recommendations.add(it) }
        }

        if (scores.sajuComplement >= (scoreData.scoreThresholds["score_high_threshold_2"] ?: 15)) {
            detailedRecs["high_saju_complement"]?.let { recommendations.add(it) }
        }

        if (scores.yinYangBalance >= (scoreData.scoreThresholds["score_high_threshold_1"] ?: 7)) {
            detailedRecs["high_yin_yang"]?.let { recommendations.add(it) }
        }

        // 주의사항
        if (scores.nameElementHarmony < (scoreData.scoreThresholds["score_low_threshold_2"] ?: 5)) {
            detailedRecs["low_element_harmony"]?.let { recommendations.add(it) }
        }

        return recommendations
    }

    fun getOverallAssessment(scores: NameScore): String {
        val grade = getGrade(scores.total)
        val assessments = scoreData.gradeAssessments

        return assessments[grade] ?: strings.errorMessage
    }

    private fun analyzeStrengths(scores: NameScore): List<String> {
        val strengths = mutableListOf<String>()
        val thresholds = scoreData.scoreThresholds
        val messages = scoreData.strengthMessages

        if (scores.fourTypesLuck >= (thresholds["score_high_threshold_1"] ?: 7)) {
            messages["four_types_luck"]?.let { strengths.add(it) }
        }
        if (scores.sajuComplement >= (thresholds["score_high_threshold_2"] ?: 15)) {
            messages["saju_complement"]?.let { strengths.add(it) }
        }
        if (scores.nameElementHarmony >= (thresholds["score_high_threshold_3"] ?: 10)) {
            messages["name_element_harmony"]?.let { strengths.add(it) }
        }
        if (scores.yinYangBalance >= (thresholds["score_high_threshold_1"] ?: 7)) {
            messages["yin_yang_balance"]?.let { strengths.add(it) }
        }

        return strengths
    }

    private fun analyzeWeaknesses(scores: NameScore): List<String> {
        val weaknesses = mutableListOf<String>()
        val thresholds = scoreData.scoreThresholds
        val messages = scoreData.weaknessMessages

        if (scores.fourTypesLuck < (thresholds["score_low_threshold_1"] ?: 5)) {
            messages["four_types_luck"]?.let { weaknesses.add(it) }
        }
        if (scores.nameElementHarmony < (thresholds["score_low_threshold_2"] ?: 5)) {
            messages["name_element_harmony"]?.let { weaknesses.add(it) }
        }
        if (scores.yinYangBalance == (thresholds["score_low_threshold_2"] ?: 5)) {
            messages["yin_yang_balance"]?.let { weaknesses.add(it) }
        }

        return weaknesses
    }

    private fun buildEvaluation(strengths: List<String>, weaknesses: List<String>): String {
        val strengthsText = if (strengths.isNotEmpty()) {
            strengths.joinToString("\n") { "${strings.listPrefix}$it" }
        } else {
            strings.defaultStrengths
        }

        val weaknessesText = if (weaknesses.isNotEmpty()) {
            weaknesses.joinToString("\n") { "${strings.listPrefix}$it" }
        } else {
            strings.defaultWeaknesses
        }

        return strings.evaluationFormat
            .replace("{strengths}", strengthsText)
            .replace("{weaknesses}", weaknessesText)
    }
}