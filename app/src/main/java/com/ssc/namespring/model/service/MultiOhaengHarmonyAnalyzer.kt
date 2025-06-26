// model/service/MultiOhaengHarmonyAnalyzer.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.saju.SajuConstants
import com.ssc.namespring.model.common.naming.NamingCalculationConstants

class MultiOhaengHarmonyAnalyzer(private val cacheManager: CacheManager) {

    fun checkBaleumOhaengHarmony(baleumOhaengCombination: String): Boolean {
        return cacheManager.baleumOhaengHarmonyCache.getOrPut(baleumOhaengCombination) {
            var johwaScore = NamingCalculationConstants.FourPillarAnalysis.MIN_HARMONY_SCORE
            for (i in NamingCalculationConstants.FourPillarAnalysis.START_INDEX until baleumOhaengCombination.length) {
                val prevElement = SajuConstants.OHAENG_SUNSE.indexOf(
                    baleumOhaengCombination[i - NamingCalculationConstants.FourPillarAnalysis.PREVIOUS_INDEX_OFFSET].toString()
                )
                val currElement = SajuConstants.OHAENG_SUNSE.indexOf(baleumOhaengCombination[i].toString())
                val diff = (currElement - prevElement + SajuConstants.Relations.ELEMENT_COUNT) %
                        SajuConstants.Relations.ELEMENT_COUNT

                when (diff) {
                    SajuConstants.Relations.GENERATING_FORWARD,
                    SajuConstants.Relations.GENERATING_BACKWARD -> johwaScore++
                    SajuConstants.Relations.CONFLICTING_FORWARD,
                    SajuConstants.Relations.CONFLICTING_BACKWARD -> return@getOrPut false
                }
            }
            johwaScore >= NamingCalculationConstants.FourPillarAnalysis.MIN_REQUIRED_SCORE
        }
    }

    fun checkHoeksuOhaengHarmony(hoeksuOhaeng: List<Int>, isComplexSurnameSingleName: Boolean): Boolean {
        var johwaScore = NamingCalculationConstants.FourPillarAnalysis.MIN_HARMONY_SCORE
        for (k in NamingCalculationConstants.FourPillarAnalysis.START_INDEX until hoeksuOhaeng.size) {
            when (hoeksuOhaeng[k] - hoeksuOhaeng[k - NamingCalculationConstants.FourPillarAnalysis.PREVIOUS_INDEX_OFFSET]) {
                NamingCalculationConstants.OhaengHarmonyScores.CONFLICTING_FORWARD_DIFF,
                NamingCalculationConstants.OhaengHarmonyScores.CONFLICTING_BACKWARD_DIFF -> if (!isComplexSurnameSingleName) return false
                NamingCalculationConstants.OhaengHarmonyScores.GENERATING_FORWARD_DIFF,
                NamingCalculationConstants.OhaengHarmonyScores.GENERATING_BACKWARD_DIFF -> johwaScore++
            }
        }
        return isComplexSurnameSingleName || johwaScore >= NamingCalculationConstants.FourPillarAnalysis.MIN_REQUIRED_SCORE
    }

    fun checkSagyeokSuriOhaengHarmony(sagyeokSuriOhaeng: List<Int>, isComplexSurnameSingleName: Boolean): Boolean {
        var johwaScore = NamingCalculationConstants.FourPillarAnalysis.MIN_HARMONY_SCORE
        for (k in NamingCalculationConstants.FourPillarAnalysis.START_INDEX until NamingCalculationConstants.FourPillarAnalysis.FOUR_TYPES_COUNT) {
            when (sagyeokSuriOhaeng[k - NamingCalculationConstants.FourPillarAnalysis.PREVIOUS_INDEX_OFFSET] - sagyeokSuriOhaeng[k]) {
                NamingCalculationConstants.OhaengHarmonyScores.CONFLICTING_FORWARD_DIFF,
                NamingCalculationConstants.OhaengHarmonyScores.CONFLICTING_BACKWARD_DIFF -> if (!isComplexSurnameSingleName) return false
                NamingCalculationConstants.OhaengHarmonyScores.GENERATING_FORWARD_DIFF,
                NamingCalculationConstants.OhaengHarmonyScores.GENERATING_BACKWARD_DIFF -> johwaScore++
            }
        }
        return isComplexSurnameSingleName || johwaScore >= NamingCalculationConstants.FourPillarAnalysis.MIN_REQUIRED_SCORE
    }

    fun analyzeOhaengRelations(ohaengCombination: String): Pair<List<Pair<String, String>>, List<Pair<String, String>>> {
        val conflictingPairs = mutableListOf<Pair<String, String>>()
        val generatingPairs = mutableListOf<Pair<String, String>>()

        for (i in 1 until ohaengCombination.length) {
            val prevElement = ohaengCombination[i - 1].toString()
            val currElement = ohaengCombination[i].toString()
            val prevIndex = SajuConstants.OHAENG_SUNSE.indexOf(prevElement)
            val currIndex = SajuConstants.OHAENG_SUNSE.indexOf(currElement)
            val diff = (currIndex - prevIndex + SajuConstants.Relations.ELEMENT_COUNT) %
                    SajuConstants.Relations.ELEMENT_COUNT

            when (diff) {
                SajuConstants.Relations.GENERATING_FORWARD,
                SajuConstants.Relations.GENERATING_BACKWARD -> 
                    generatingPairs.add(prevElement to currElement)
                SajuConstants.Relations.CONFLICTING_FORWARD,
                SajuConstants.Relations.CONFLICTING_BACKWARD -> 
                    conflictingPairs.add(prevElement to currElement)
            }
        }

        return conflictingPairs to generatingPairs
    }
}
