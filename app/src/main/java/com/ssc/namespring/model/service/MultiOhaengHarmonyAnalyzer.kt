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
                Constants.OhaengHarmonyScores.CONFLICTING_FORWARD_DIFF,
                Constants.OhaengHarmonyScores.CONFLICTING_BACKWARD_DIFF -> if (!isComplexSurnameSingleName) return false
                Constants.OhaengHarmonyScores.GENERATING_FORWARD_DIFF,
                Constants.OhaengHarmonyScores.GENERATING_BACKWARD_DIFF -> johwaScore++
            }
        }
        return isComplexSurnameSingleName || johwaScore >= Constants.FourPillarAnalysis.MIN_REQUIRED_SCORE
    }

    fun checkSagyeokSuriOhaengHarmony(sagyeokSuriOhaeng: List<Int>, isComplexSurnameSingleName: Boolean): Boolean {
        var johwaScore = Constants.FourPillarAnalysis.MIN_HARMONY_SCORE
        for (k in Constants.FourPillarAnalysis.START_INDEX until Constants.FourPillarAnalysis.FOUR_TYPES_COUNT) {
            when (sagyeokSuriOhaeng[k - Constants.FourPillarAnalysis.PREVIOUS_INDEX_OFFSET] - sagyeokSuriOhaeng[k]) {
                Constants.OhaengHarmonyScores.CONFLICTING_FORWARD_DIFF,
                Constants.OhaengHarmonyScores.CONFLICTING_BACKWARD_DIFF -> if (!isComplexSurnameSingleName) return false
                Constants.OhaengHarmonyScores.GENERATING_FORWARD_DIFF,
                Constants.OhaengHarmonyScores.GENERATING_BACKWARD_DIFF -> johwaScore++
            }
        }
        return isComplexSurnameSingleName || johwaScore >= Constants.FourPillarAnalysis.MIN_REQUIRED_SCORE
    }
}
