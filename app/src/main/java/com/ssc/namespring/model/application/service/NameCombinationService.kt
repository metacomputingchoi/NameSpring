// model/application/service/NameCombinationService.kt
package com.ssc.namespring.model.application.service

import com.ssc.namespring.model.common.constants.StrokeConstants
import com.ssc.namespring.model.common.util.HangulUtils
import com.ssc.namespring.model.domain.name.entity.NameCombination
import com.ssc.namespring.model.domain.name.entity.ElementCheck

class NameCombinationService(private val hanja2Stroke: Map<String, Int>) {

    fun generateCombinations(surHangul: String, surHanja: String): List<NameCombination> {
        val surHangulStroke = HangulUtils.getHangulStrokeCount(surHangul[0])
        val surHanjaStroke = hanja2Stroke[surHanja] ?: 0
        val results = mutableListOf<NameCombination>()
        val luck = IntArray(StrokeConstants.LUCK_ARRAY_SIZE) { 0 }
        StrokeConstants.GOOD_LUCK.forEach { luck[it] = 1 }

        for (stroke1 in StrokeConstants.MIN_STROKE..StrokeConstants.MAX_STROKE) {
            for (stroke2 in StrokeConstants.MIN_STROKE..StrokeConstants.MAX_STROKE) {
                val fourTypes = listOf(
                    stroke1 + stroke2,
                    surHanjaStroke + stroke1,
                    surHanjaStroke + stroke2,
                    (surHanjaStroke + stroke1 + stroke2) % StrokeConstants.MODULO_VALUE
                )

                var score = fourTypes.sumOf { if (it < StrokeConstants.LUCK_ARRAY_SIZE) luck[it] else 0 }
                val namePn = listOf(surHanjaStroke % 2, stroke1 % 2, stroke2 % 2)
                val namePnSum = namePn.sum()

                if (namePnSum == StrokeConstants.NAME_PN_SUM_INVALID_ZERO ||
                    namePnSum == StrokeConstants.NAME_PN_SUM_INVALID_THREE) {
                    score = 0
                }

                var nameElements = listOf(
                    (surHanjaStroke % StrokeConstants.ELEMENT_NORMALIZE_VALUE) +
                            (surHanjaStroke % StrokeConstants.ELEMENT_NORMALIZE_VALUE) % 2,
                    (stroke1 % StrokeConstants.ELEMENT_NORMALIZE_VALUE) +
                            (stroke1 % StrokeConstants.ELEMENT_NORMALIZE_VALUE) % 2,
                    (stroke2 % StrokeConstants.ELEMENT_NORMALIZE_VALUE) +
                            (stroke2 % StrokeConstants.ELEMENT_NORMALIZE_VALUE) % 2
                ).map { if (it == StrokeConstants.ELEMENT_NORMALIZE_VALUE) 0 else it }

                var scoreCoexistName = 0
                val nameElementChecks = mutableListOf<ElementCheck>()
                var scoreZeroReason: String? = null

                for (k in StrokeConstants.ELEMENT_LOOP_START..StrokeConstants.ELEMENT_LOOP_END) {
                    val diff = nameElements[k] - nameElements[k - 1]
                    val result = when (diff) {
                        StrokeConstants.ELEMENT_DIFF_CONFLICT_1,
                        StrokeConstants.ELEMENT_DIFF_CONFLICT_2 -> {
                            score = 0
                            scoreZeroReason = scoreZeroReason ?: "name_elements_conflict_at_${k-1}_$k"
                            "conflicting"
                        }
                        StrokeConstants.ELEMENT_DIFF_HARMONY_1,
                        StrokeConstants.ELEMENT_DIFF_HARMONY_2 -> {
                            scoreCoexistName++
                            "harmonious"
                        }
                        else -> "neutral"
                    }

                    nameElementChecks.add(ElementCheck(
                        position = "${k-1}-$k",
                        elements = "${nameElements[k-1]}-${nameElements[k]}",
                        diff = diff,
                        result = result
                    ))
                }

                if (scoreCoexistName == 0) {
                    score = 0
                    scoreZeroReason = scoreZeroReason ?: "no_name_element_harmony"
                }

                var typeElements = fourTypes.map { ft ->
                    val te = (ft % StrokeConstants.ELEMENT_NORMALIZE_VALUE) +
                            (ft % StrokeConstants.ELEMENT_NORMALIZE_VALUE) % 2
                    if (te == StrokeConstants.ELEMENT_NORMALIZE_VALUE) 0 else te
                }

                var scoreCoexistType = 0
                val typeElementChecks = mutableListOf<ElementCheck>()

                for (k in StrokeConstants.TYPE_ELEMENT_LOOP_START..StrokeConstants.TYPE_ELEMENT_LOOP_END) {
                    val diff = typeElements[k - 1] - typeElements[k]
                    val result = when (diff) {
                        StrokeConstants.ELEMENT_DIFF_CONFLICT_1,
                        StrokeConstants.ELEMENT_DIFF_CONFLICT_2 -> {
                            score = 0
                            scoreZeroReason = scoreZeroReason ?: "type_elements_conflict_at_${k-1}_$k"
                            "conflicting"
                        }
                        StrokeConstants.ELEMENT_DIFF_HARMONY_1,
                        StrokeConstants.ELEMENT_DIFF_HARMONY_2 -> {
                            scoreCoexistType++
                            "harmonious"
                        }
                        else -> "neutral"
                    }

                    typeElementChecks.add(ElementCheck(
                        position = "${k-1}-$k",
                        elements = "${typeElements[k-1]}-${typeElements[k]}",
                        diff = diff,
                        result = result
                    ))
                }

                if (scoreCoexistType == 0) {
                    score = 0
                    scoreZeroReason = scoreZeroReason ?: "no_type_element_harmony"
                }

                val finalScore = score

                if (finalScore >= StrokeConstants.MIN_FINAL_SCORE) {
                    results.add(NameCombination(
                        surHanjaStroke = surHanjaStroke,
                        stroke1 = stroke1,
                        stroke2 = stroke2,
                        fourTypes = fourTypes,
                        fourTypesLuck = fourTypes.map { if (it < StrokeConstants.LUCK_ARRAY_SIZE) luck[it] else 0 },
                        initialScore = score,
                        namePn = namePn,
                        namePnSum = namePnSum,
                        nameElements = nameElements,
                        nameElementChecks = nameElementChecks,
                        scoreCoexistName = scoreCoexistName,
                        typeElements = typeElements,
                        typeElementChecks = typeElementChecks,
                        scoreCoexistType = scoreCoexistType,
                        finalScore = finalScore,
                        scoreZeroReason = scoreZeroReason
                    ))
                }
            }
        }

        return results
    }
}