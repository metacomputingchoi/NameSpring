// model/service/MultiOhaengHarmonyAnalyzer.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.saju.SajuConstants
import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.util.OhaengCalculationUtils
import com.ssc.namespring.model.util.OhaengCalculationUtils.OhaengRelation

class MultiOhaengHarmonyAnalyzer(
    private val cacheManager: CacheManager
) {

    fun checkBaleumOhaengHarmony(baleumOhaengCombination: String): Boolean {
        return cacheManager.baleumOhaengHarmonyCache.getOrPut(baleumOhaengCombination) {
            checkStringOhaengHarmony(baleumOhaengCombination)
        }
    }

    fun checkHoeksuOhaengHarmony(hoeksuOhaeng: List<Int>, isComplexSurnameSingleName: Boolean): Boolean {
        return checkNumericOhaengHarmony(hoeksuOhaeng, isComplexSurnameSingleName)
    }

    fun checkSagyeokSuriOhaengHarmony(sagyeokSuriOhaeng: List<Int>, isComplexSurnameSingleName: Boolean): Boolean {
        val differences = (1 until NamingCalculationConstants.FourPillarAnalysis.FOUR_TYPES_COUNT).map { k ->
            sagyeokSuriOhaeng[k - 1] - sagyeokSuriOhaeng[k]
        }

        return checkHarmonyByDifferences(differences, isComplexSurnameSingleName)
    }

    fun analyzeOhaengRelations(ohaengCombination: String): Pair<List<Pair<String, String>>, List<Pair<String, String>>> {
        val conflictingPairs = mutableListOf<Pair<String, String>>()
        val generatingPairs = mutableListOf<Pair<String, String>>()

        for (i in 1 until ohaengCombination.length) {
            val prevElement = ohaengCombination[i - 1].toString()
            val currElement = ohaengCombination[i].toString()
            val diff = OhaengCalculationUtils.calculateOhaengDifferenceByString(prevElement, currElement)

            when (OhaengCalculationUtils.getOhaengRelation(diff)) {
                OhaengRelation.GENERATING -> generatingPairs.add(prevElement to currElement)
                OhaengRelation.CONFLICTING -> conflictingPairs.add(prevElement to currElement)
                OhaengRelation.NEUTRAL -> { /* 중립 관계는 무시 */ }
            }
        }

        return conflictingPairs to generatingPairs
    }

    private fun checkStringOhaengHarmony(ohaengCombination: String): Boolean {
        var johwaScore = NamingCalculationConstants.FourPillarAnalysis.MIN_HARMONY_SCORE

        for (i in 1 until ohaengCombination.length) {
            val diff = OhaengCalculationUtils.calculateOhaengDifferenceByString(
                ohaengCombination[i - 1].toString(),
                ohaengCombination[i].toString()
            )

            when (OhaengCalculationUtils.getOhaengRelation(diff)) {
                OhaengRelation.GENERATING -> johwaScore++
                OhaengRelation.CONFLICTING -> return false
                OhaengRelation.NEUTRAL -> { /* 중립 관계는 무시 */ }
            }
        }

        return johwaScore >= NamingCalculationConstants.FourPillarAnalysis.MIN_REQUIRED_SCORE
    }

    private fun checkNumericOhaengHarmony(hoeksuOhaeng: List<Int>, isComplexSurnameSingleName: Boolean): Boolean {
        val differences = (1 until hoeksuOhaeng.size).map { i ->
            hoeksuOhaeng[i] - hoeksuOhaeng[i - 1]
        }

        return checkHarmonyByDifferences(differences, isComplexSurnameSingleName)
    }

    private fun checkHarmonyByDifferences(differences: List<Int>, isComplexSurnameSingleName: Boolean): Boolean {
        var johwaScore = NamingCalculationConstants.FourPillarAnalysis.MIN_HARMONY_SCORE

        val conflictingValues = setOf(
            NamingCalculationConstants.OhaengHarmonyScores.CONFLICTING_FORWARD_DIFF,
            NamingCalculationConstants.OhaengHarmonyScores.CONFLICTING_BACKWARD_DIFF
        )

        val generatingValues = setOf(
            NamingCalculationConstants.OhaengHarmonyScores.GENERATING_FORWARD_DIFF,
            NamingCalculationConstants.OhaengHarmonyScores.GENERATING_BACKWARD_DIFF
        )

        for (diff in differences) {
            when (diff) {
                in conflictingValues -> if (!isComplexSurnameSingleName) return false
                in generatingValues -> johwaScore++
            }
        }

        return isComplexSurnameSingleName ||
                johwaScore >= NamingCalculationConstants.FourPillarAnalysis.MIN_REQUIRED_SCORE
    }
}