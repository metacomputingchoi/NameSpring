// model/service/NameCombinationAnalyzer.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.Constants
import com.ssc.namespring.model.data.FourTypes
import com.ssc.namespring.model.data.GoodCombination
import com.ssc.namespring.model.util.cartesianProduct

class NameCombinationAnalyzer(
    private val strokeAnalyzer: StrokeAnalyzer,
    private val harmonyAnalyzer: HarmonyAnalyzer
) {

    fun analyzeNameCombinations(
        surHangul: String,
        surHanja: String,
        nameLength: Int
    ): List<GoodCombination> {
        val surLength = surHanja.length
        val surHanjaStrokes = surHanja.map {
            strokeAnalyzer.getHanjaStrokeCount(it.toString()) ?: 0
        }

        val strokeRanges = buildList {
            addAll(surHanjaStrokes.map { listOf(it) })
            repeat(nameLength) { add((Constants.MIN_STROKE..Constants.MAX_STROKE).toList()) }
        }

        val isComplexSurnameSingleName = surLength >= 2 && nameLength == 1

        val indexRanges: List<List<Int>> = strokeRanges.map { range ->
            range.indices.toList()
        }

        return cartesianProduct(indexRanges).mapNotNull { indices ->
            val strokeValues = indices.mapIndexed { i, idx -> strokeRanges[i][idx] }

            val fourTypes = calculateFourTypes(strokeValues, surLength)
            val score = fourTypes.getValues().count { it in Constants.GOOD_LUCK }

            val namePn = strokeValues.map { it % Constants.YIN_YANG_MODULO }

            // 음양 체크
            if (!isComplexSurnameSingleName &&
                (namePn.sum() == 0 || namePn.sum() == namePn.size)) {
                return@mapNotNull null
            }

            val nameElements = strokeValues.map { sv ->
                val ne = (sv % Constants.STROKE_MODULO) +
                        (sv % Constants.STROKE_MODULO) % Constants.YIN_YANG_MODULO
                if (ne == Constants.STROKE_MODULO) 0 else ne
            }

            // 오행 체크
            if (!harmonyAnalyzer.checkElementsHarmony(nameElements, isComplexSurnameSingleName)) {
                return@mapNotNull null
            }

            val typeElements = fourTypes.getValues().map { ft ->
                val te = (ft % Constants.STROKE_MODULO) +
                        (ft % Constants.STROKE_MODULO) % Constants.YIN_YANG_MODULO
                if (te == Constants.STROKE_MODULO) 0 else te
            }

            // 사격 오행 체크
            if (!harmonyAnalyzer.checkTypeElementsHarmony(typeElements, isComplexSurnameSingleName)) {
                return@mapNotNull null
            }

            val minScore = getMinScore(isComplexSurnameSingleName, surLength, nameLength)

            if (score >= minScore) {
                GoodCombination(
                    nameStrokes = strokeValues,
                    fourTypes = fourTypes,
                    namePN = namePn,
                    nameElements = nameElements,
                    typeElements = typeElements
                )
            } else null
        }
    }

    private fun calculateFourTypes(strokeValues: List<Int>, surLength: Int): FourTypes {
        val hyung = strokeValues.subList(surLength, strokeValues.size).sum()
        val won = strokeValues[surLength - 1] + strokeValues[surLength]
        val i = strokeValues.first() + strokeValues.last()
        val jung = strokeValues.sum() % Constants.JUNG_MODULO

        return FourTypes(hyung, won, i, jung)
    }

    private fun getMinScore(
        isComplexSurnameSingleName: Boolean,
        surLength: Int,
        nameLength: Int
    ): Int {
        return when {
            isComplexSurnameSingleName -> Constants.MinScores.COMPLEX_SURNAME_SINGLE_NAME
            surLength == 1 && nameLength == 1 -> Constants.MinScores.SINGLE_SURNAME_SINGLE_NAME
            else -> Constants.MinScores.DEFAULT
        }
    }
}