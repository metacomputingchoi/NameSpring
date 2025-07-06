// utils/analysis/StrokeAnalyzer.kt
package com.ssc.namespring.utils.analysis

import com.ssc.namespring.model.data.source.SimpleStrokeMeaning
import com.ssc.namespring.utils.data.json.JsonDataRepository

internal class StrokeAnalyzer(private val repository: JsonDataRepository) {

    fun getStrokeMeaning(stroke: Int): SimpleStrokeMeaning {
        val normalizedStroke = normalizeStroke(stroke)
        val strokeStr = normalizedStroke.toString()

        // StrokeMeanings는 상세 버전이므로, 간단한 버전으로 변환
        val detailed = repository.strokeMeanings.strokeMeanings[strokeStr]
            ?: repository.strokeMeanings.strokeMeanings["1"]

        return if (detailed != null) {
            SimpleStrokeMeaning(
                strokes = detailed.number,
                luck = detailed.luckyLevel,
                element = "수", // 기본값, 실제 JSON에 없으면 다른 방법으로 결정
                character = detailed.title,
                meaning = detailed.summary
            )
        } else {
            SimpleStrokeMeaning(
                strokes = 1,
                luck = "평범",
                element = "수",
                character = "시작",
                meaning = "기본 의미"
            )
        }
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
        val thresholds = repository.scoreEvaluations.scoreThresholds
        return when {
            score >= thresholds.gradeA -> "A"
            score >= thresholds.gradeB -> "B"
            score >= thresholds.gradeC -> "C"
            else -> "D"
        }
    }

    private fun normalizeStroke(stroke: Int): Int {
        return if (stroke > 81) stroke % 81 else stroke
    }
}