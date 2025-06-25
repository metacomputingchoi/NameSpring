// model/service/HarmonyAnalyzer.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.Constants

class HarmonyAnalyzer(private val cacheManager: CacheManager) {

    fun isHarmoniousElementCombination(elementCombination: String): Boolean {
        return cacheManager.harmoniousCache.getOrPut(elementCombination) {
            var scoreCoexist = Constants.FourPillarAnalysis.MIN_HARMONY_SCORE
            for (i in Constants.FourPillarAnalysis.START_INDEX until elementCombination.length) {
                val prevElement = Constants.ELEMENTS_ORDER.indexOf(
                    elementCombination[i - Constants.FourPillarAnalysis.PREVIOUS_INDEX_OFFSET].toString()
                )
                val currElement = Constants.ELEMENTS_ORDER.indexOf(elementCombination[i].toString())
                val diff = (currElement - prevElement + Constants.ElementRelations.ELEMENT_COUNT) %
                        Constants.ElementRelations.ELEMENT_COUNT

                when (diff) {
                    Constants.ElementRelations.GENERATING_FORWARD,
                    Constants.ElementRelations.GENERATING_BACKWARD -> scoreCoexist++
                    Constants.ElementRelations.CONFLICTING_FORWARD,
                    Constants.ElementRelations.CONFLICTING_BACKWARD -> return@getOrPut false
                }
            }
            scoreCoexist >= Constants.FourPillarAnalysis.MIN_REQUIRED_SCORE
        }
    }

    fun checkElementsHarmony(elements: List<Int>, isComplexSurnameSingleName: Boolean): Boolean {
        var scoreCoexist = Constants.FourPillarAnalysis.MIN_HARMONY_SCORE
        for (k in Constants.FourPillarAnalysis.START_INDEX until elements.size) {
            when (elements[k] - elements[k - Constants.FourPillarAnalysis.PREVIOUS_INDEX_OFFSET]) {
                Constants.HarmonyScores.CONFLICTING_FORWARD_DIFF,
                Constants.HarmonyScores.CONFLICTING_BACKWARD_DIFF -> if (!isComplexSurnameSingleName) return false
                Constants.HarmonyScores.GENERATING_FORWARD_DIFF,
                Constants.HarmonyScores.GENERATING_BACKWARD_DIFF -> scoreCoexist++
            }
        }
        return isComplexSurnameSingleName || scoreCoexist >= Constants.FourPillarAnalysis.MIN_REQUIRED_SCORE
    }

    fun checkTypeElementsHarmony(typeElements: List<Int>, isComplexSurnameSingleName: Boolean): Boolean {
        var scoreCoexist = Constants.FourPillarAnalysis.MIN_HARMONY_SCORE
        for (k in Constants.FourPillarAnalysis.START_INDEX until Constants.FourPillarAnalysis.FOUR_TYPES_COUNT) {
            when (typeElements[k - Constants.FourPillarAnalysis.PREVIOUS_INDEX_OFFSET] - typeElements[k]) {
                Constants.HarmonyScores.CONFLICTING_FORWARD_DIFF,
                Constants.HarmonyScores.CONFLICTING_BACKWARD_DIFF -> if (!isComplexSurnameSingleName) return false
                Constants.HarmonyScores.GENERATING_FORWARD_DIFF,
                Constants.HarmonyScores.GENERATING_BACKWARD_DIFF -> scoreCoexist++
            }
        }
        return isComplexSurnameSingleName || scoreCoexist >= Constants.FourPillarAnalysis.MIN_REQUIRED_SCORE
    }
}