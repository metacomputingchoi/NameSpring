// model/service/HarmonyAnalyzer.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.Constants

class HarmonyAnalyzer(private val cacheManager: CacheManager) {

    fun isHarmoniousElementCombination(elementCombination: String): Boolean {
        return cacheManager.harmoniousCache.getOrPut(elementCombination) {
            var scoreCoexist = 0
            for (i in 1 until elementCombination.length) {
                val prevElement = Constants.ELEMENTS_ORDER.indexOf(elementCombination[i - 1].toString())
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
            scoreCoexist > 0
        }
    }

    fun checkElementsHarmony(elements: List<Int>, isComplexSurnameSingleName: Boolean): Boolean {
        var scoreCoexist = 0
        for (k in 1 until elements.size) {
            when (elements[k] - elements[k - 1]) {
                4, -6 -> if (!isComplexSurnameSingleName) return false
                2, -8 -> scoreCoexist++
            }
        }
        return isComplexSurnameSingleName || scoreCoexist > 0
    }

    fun checkTypeElementsHarmony(typeElements: List<Int>, isComplexSurnameSingleName: Boolean): Boolean {
        var scoreCoexist = 0
        for (k in 1..3) {
            when (typeElements[k - 1] - typeElements[k]) {
                4, -6 -> if (!isComplexSurnameSingleName) return false
                2, -8 -> scoreCoexist++
            }
        }
        return isComplexSurnameSingleName || scoreCoexist > 0
    }
}