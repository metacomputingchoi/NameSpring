// model/analyzer/NameCombinationAnalyzer.kt
package com.ssc.namespring.model.analyzer

import com.ssc.namespring.model.constants.Constants
import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.utils.NameUtils

class NameCombinationAnalyzer(private val hanja2Stroke: Map<String, Int>) {

    fun analyzeNameCombinations(surHangul: String, surHanja: String): List<Map<String, Any>> {
        val surHangulStroke = NameUtils.getHangulStrokeCount(surHangul[0])
        val surHanjaStroke = hanja2Stroke[surHanja] ?: 0
        val results = mutableListOf<Map<String, Any>>()
        val luck = IntArray(82) { 0 }
        Constants.GOOD_LUCK.forEach { luck[it] = 1 }

        for (stroke1 in 1..27) {
            for (stroke2 in 1..27) {
                val fourTypes = listOf(
                    stroke1 + stroke2,
                    surHanjaStroke + stroke1,
                    surHanjaStroke + stroke2,
                    (surHanjaStroke + stroke1 + stroke2) % 81
                )

                var score = fourTypes.sumOf { if (it < 82) luck[it] else 0 }
                val namePn = listOf(surHanjaStroke % 2, stroke1 % 2, stroke2 % 2)

                val analysisDetails = mutableMapOf<String, Any>(
                    "sur_hanja_stroke" to surHanjaStroke,
                    "stroke1" to stroke1,
                    "stroke2" to stroke2,
                    "four_types" to fourTypes,
                    "four_types_luck" to fourTypes.map { if (it < 82) luck[it] else 0 },
                    "initial_score" to score,
                    "name_pn" to namePn,
                    "name_pn_sum" to namePn.sum()
                )

                if (namePn.sum() == 0 || namePn.sum() == 3) {
                    score = 0
                    analysisDetails["score_zero_reason"] = "name_pn_sum_is_0_or_3"
                }

                var nameElements = listOf(
                    (surHanjaStroke % 10) + (surHanjaStroke % 10) % 2,
                    (stroke1 % 10) + (stroke1 % 10) % 2,
                    (stroke2 % 10) + (stroke2 % 10) % 2
                ).map { if (it == 10) 0 else it }

                analysisDetails["name_elements"] = nameElements

                var scoreCoexistName = 0
                val nameElementChecks = mutableListOf<Map<String, Any>>()

                for (k in 1..2) {
                    val diff = nameElements[k] - nameElements[k - 1]
                    val checkInfo = mutableMapOf<String, Any>(
                        "position" to "${k-1}-$k",
                        "elements" to "${nameElements[k-1]}-${nameElements[k]}",
                        "diff" to diff
                    )

                    when (diff) {
                        4, -6 -> {
                            score = 0
                            checkInfo["result"] = "conflicting"
                            analysisDetails["score_zero_reason"] = "name_elements_conflict_at_${k-1}_$k"
                        }
                        2, -8 -> {
                            scoreCoexistName++
                            checkInfo["result"] = "harmonious"
                        }
                        else -> checkInfo["result"] = "neutral"
                    }
                    nameElementChecks.add(checkInfo)
                }

                analysisDetails["name_element_checks"] = nameElementChecks
                analysisDetails["score_coexist_name"] = scoreCoexistName

                if (scoreCoexistName == 0) {
                    score = 0
                    analysisDetails["score_zero_reason"] = "no_name_element_harmony"
                }

                var typeElements = fourTypes.map { ft ->
                    val te = (ft % 10) + (ft % 10) % 2
                    if (te == 10) 0 else te
                }
                analysisDetails["type_elements"] = typeElements

                var scoreCoexistType = 0
                val typeElementChecks = mutableListOf<Map<String, Any>>()

                for (k in 1..3) {
                    val diff = typeElements[k - 1] - typeElements[k]
                    val checkInfo = mutableMapOf<String, Any>(
                        "position" to "${k-1}-$k",
                        "elements" to "${typeElements[k-1]}-${typeElements[k]}",
                        "diff" to diff
                    )

                    when (diff) {
                        4, -6 -> {
                            score = 0
                            checkInfo["result"] = "conflicting"
                            analysisDetails["score_zero_reason"] = "type_elements_conflict_at_${k-1}_$k"
                        }
                        2, -8 -> {
                            scoreCoexistType++
                            checkInfo["result"] = "harmonious"
                        }
                        else -> checkInfo["result"] = "neutral"
                    }
                    typeElementChecks.add(checkInfo)
                }

                analysisDetails["type_element_checks"] = typeElementChecks
                analysisDetails["score_coexist_type"] = scoreCoexistType

                if (scoreCoexistType == 0) {
                    score = 0
                    analysisDetails["score_zero_reason"] = "no_type_element_harmony"
                }

                analysisDetails["final_score"] = score

                if (score >= 4) {
                    results.add(mapOf(
                        "stroke1" to stroke1,
                        "stroke2" to stroke2,
                        "analysis_details" to analysisDetails
                    ))
                }
            }
        }

        return results
    }

    fun mapToCombinationAnalysis(map: Map<*, *>): CombinationAnalysis {
        @Suppress("UNCHECKED_CAST")
        return CombinationAnalysis(
            surHanjaStroke = map["sur_hanja_stroke"] as Int,
            stroke1 = map["stroke1"] as Int,
            stroke2 = map["stroke2"] as Int,
            fourTypes = map["four_types"] as List<Int>,
            fourTypesLuck = map["four_types_luck"] as List<Int>,
            initialScore = map["initial_score"] as Int,
            namePn = map["name_pn"] as List<Int>,
            namePnSum = map["name_pn_sum"] as Int,
            nameElements = map["name_elements"] as List<Int>,
            nameElementChecks = (map["name_element_checks"] as List<Map<String, Any>>).map {
                ElementCheck(
                    position = it["position"] as String,
                    elements = it["elements"] as String,
                    diff = it["diff"] as Int,
                    result = it["result"] as String
                )
            },
            scoreCoexistName = map["score_coexist_name"] as Int,
            typeElements = map["type_elements"] as List<Int>,
            typeElementChecks = (map["type_element_checks"] as List<Map<String, Any>>).map {
                ElementCheck(
                    position = it["position"] as String,
                    elements = it["elements"] as String,
                    diff = it["diff"] as Int,
                    result = it["result"] as String
                )
            },
            scoreCoexistType = map["score_coexist_type"] as Int,
            finalScore = map["final_score"] as Int,
            scoreZeroReason = map["score_zero_reason"] as? String
        )
    }
}
