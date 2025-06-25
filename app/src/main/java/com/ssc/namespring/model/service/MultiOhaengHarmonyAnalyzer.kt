// model/service/MultiOhaengHarmonyAnalyzer.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.Constants

class MultiOhaengHarmonyAnalyzer(private val cacheManager: CacheManager) {

    fun checkBaleumOhaengHarmony(baleumOhaengCombination: String): Boolean {
        return cacheManager.baleumOhaengHarmonyCache.getOrPut(baleumOhaengCombination) {
            var johwaScore = Constants.FourPillarAnalysis.MIN_HARMONY_SCORE
            for (i in Constants.FourPillarAnalysis.START_INDEX until baleumOhaengCombination.length) {
                val prevElement = Constants.OHAENG_SUNSE.indexOf(
                    baleumOhaengCombination[i - Constants.FourPillarAnalysis.PREVIOUS_INDEX_OFFSET].toString()
                )
                val currElement = Constants.OHAENG_SUNSE.indexOf(baleumOhaengCombination[i].toString())
                val diff = (currElement - prevElement + Constants.SangsaengSanggeukRelations.ELEMENT_COUNT) %
                        Constants.SangsaengSanggeukRelations.ELEMENT_COUNT

                when (diff) {
                    Constants.SangsaengSanggeukRelations.GENERATING_FORWARD,
                    Constants.SangsaengSanggeukRelations.GENERATING_BACKWARD -> johwaScore++
                    Constants.SangsaengSanggeukRelations.CONFLICTING_FORWARD,
                    Constants.SangsaengSanggeukRelations.CONFLICTING_BACKWARD -> return@getOrPut false
                }
            }
            johwaScore >= Constants.FourPillarAnalysis.MIN_REQUIRED_SCORE
        }
    }

    fun checkHoeksuOhaengHarmony(hoeksuOhaeng: List<Int>, isComplexSurnameSingleName: Boolean): Boolean {
        var johwaScore = Constants.FourPillarAnalysis.MIN_HARMONY_SCORE
        for (k in Constants.FourPillarAnalysis.START_INDEX until hoeksuOhaeng.size) {
            when (hoeksuOhaeng[k] - hoeksuOhaeng[k - Constants.FourPillarAnalysis.PREVIOUS_INDEX_OFFSET]) {
                Constants.OhaengHarmonyScoresCommon.CONFLICTING_FORWARD_DIFF,
                Constants.OhaengHarmonyScoresCommon.CONFLICTING_BACKWARD_DIFF -> if (!isComplexSurnameSingleName) return false
                Constants.OhaengHarmonyScoresCommon.GENERATING_FORWARD_DIFF,
                Constants.OhaengHarmonyScoresCommon.GENERATING_BACKWARD_DIFF -> johwaScore++
            }
        }
        return isComplexSurnameSingleName || johwaScore >= Constants.FourPillarAnalysis.MIN_REQUIRED_SCORE
    }

    fun checkSagyeokSuriOhaengHarmony(sagyeokSuriOhaeng: List<Int>, isComplexSurnameSingleName: Boolean): Boolean {
        var johwaScore = Constants.FourPillarAnalysis.MIN_HARMONY_SCORE
        for (k in Constants.FourPillarAnalysis.START_INDEX until Constants.FourPillarAnalysis.FOUR_TYPES_COUNT) {
            when (sagyeokSuriOhaeng[k - Constants.FourPillarAnalysis.PREVIOUS_INDEX_OFFSET] - sagyeokSuriOhaeng[k]) {
                Constants.OhaengHarmonyScoresCommon.CONFLICTING_FORWARD_DIFF,
                Constants.OhaengHarmonyScoresCommon.CONFLICTING_BACKWARD_DIFF -> if (!isComplexSurnameSingleName) return false
                Constants.OhaengHarmonyScoresCommon.GENERATING_FORWARD_DIFF,
                Constants.OhaengHarmonyScoresCommon.GENERATING_BACKWARD_DIFF -> johwaScore++
            }
        }
        return isComplexSurnameSingleName || johwaScore >= Constants.FourPillarAnalysis.MIN_REQUIRED_SCORE
    }

    fun analyzeOhaengRelations(ohaengCombination: String): Pair<List<Pair<String, String>>, List<Pair<String, String>>> {
        val conflictingPairs = mutableListOf<Pair<String, String>>()
        val generatingPairs = mutableListOf<Pair<String, String>>()

        for (i in 1 until ohaengCombination.length) {
            val prevElement = ohaengCombination[i - 1].toString()
            val currElement = ohaengCombination[i].toString()
            val prevIndex = Constants.OHAENG_SUNSE.indexOf(prevElement)
            val currIndex = Constants.OHAENG_SUNSE.indexOf(currElement)
            val diff = (currIndex - prevIndex + Constants.SangsaengSanggeukRelations.ELEMENT_COUNT) %
                    Constants.SangsaengSanggeukRelations.ELEMENT_COUNT

            when (diff) {
                Constants.SangsaengSanggeukRelations.GENERATING_FORWARD,
                Constants.SangsaengSanggeukRelations.GENERATING_BACKWARD -> 
                    generatingPairs.add(prevElement to currElement)
                Constants.SangsaengSanggeukRelations.CONFLICTING_FORWARD,
                Constants.SangsaengSanggeukRelations.CONFLICTING_BACKWARD -> 
                    conflictingPairs.add(prevElement to currElement)
            }
        }

        return conflictingPairs to generatingPairs
    }
}
