// utils/analysis/StrokeAnalyzer.kt
package com.ssc.namespring.utils.analysis

import com.ssc.namespring.model.data.source.StrokeMeaningDetail
import com.ssc.namespring.utils.data.json.JsonDataRepository

internal class StrokeAnalyzer(private val repository: JsonDataRepository) {

    fun getStrokeMeaning(stroke: Int): StrokeMeaningDetail {
        val normalizedStroke = normalizeStroke(stroke)
        val strokeStr = normalizedStroke.toString()

        return repository.strokeMeanings.strokeMeanings[strokeStr]
            ?: repository.strokeMeanings.strokeMeanings["1"]!!
    }

    fun isBusinessLuckStroke(stroke: Int): Boolean {
        val normalizedStroke = normalizeStroke(stroke)
        return repository.businessLuckStrokes?.businessLuckStrokes?.contains(normalizedStroke) == true
    }

    fun isLeadershipStroke(stroke: Int): Boolean {
        val normalizedStroke = normalizeStroke(stroke)
        return repository.businessLuckStrokes?.leadershipStrokes?.contains(normalizedStroke) == true
    }

    fun getGrade(score: Int): String {
        return when {
            score >= repository.scoreEvaluations.scoreThresholds.gradeA -> "A"
            score >= repository.scoreEvaluations.scoreThresholds.gradeB -> "B"
            score >= repository.scoreEvaluations.scoreThresholds.gradeC -> "C"
            else -> "D"
        }
    }

    private fun normalizeStroke(stroke: Int): Int {
        return if (stroke > 81) stroke % 81 else stroke
    }
}