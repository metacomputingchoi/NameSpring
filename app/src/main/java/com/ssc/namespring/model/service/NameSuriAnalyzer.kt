// model/service/NameSuriAnalyzer.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.GoodCombination
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.util.cartesianProduct
import com.ssc.namespring.model.util.NamingCalculationUtils
import com.ssc.namespring.model.util.OhaengCalculationUtils

class NameSuriAnalyzer(
    private val hanjaHoeksuAnalyzer: HanjaHoeksuAnalyzer,
    private val multiOhaengHarmonyAnalyzer: MultiOhaengHarmonyAnalyzer
) {
    private val eumYangAnalysisService = EumYangAnalysisService()

    fun analyzeNameCombinations(
        surHangul: String,
        surHanja: String,
        nameLength: Int,
        requireMinScore: Boolean = true
    ): List<GoodCombination> {
        try {
            val surLength = surHanja.length
            val surHanjaHoeksu = surHanja.map {
                hanjaHoeksuAnalyzer.getHanjaHoeksu(it.toString()) ?: 0
            }

            val hoeksuRanges = buildList {
                addAll(surHanjaHoeksu.map { listOf(it) })
                repeat(nameLength) { add((NamingCalculationConstants.MIN_STROKE..NamingCalculationConstants.MAX_STROKE).toList()) }
            }

            val isComplexSurnameSingleName = NamingCalculationUtils.isComplexSurnameSingleName(surLength, nameLength)

            val indexRanges: List<List<Int>> = hoeksuRanges.map { range ->
                range.indices.toList()
            }

            return cartesianProduct(indexRanges).mapNotNull { indices ->
                val hanjaHoeksuValues = indices.mapIndexed { i, idx -> hoeksuRanges[i][idx] }

                val sagyeok = NamingCalculationUtils.calculateSagyeok(hanjaHoeksuValues, surLength)
                val score = NamingCalculationUtils.countGilhanHoeksu(sagyeok.getValues())

                val nameBaleumEumyang = hanjaHoeksuValues.map { it % NamingCalculationConstants.EUMYANG_MODULO }

                // 음양 체크
                if (!isComplexSurnameSingleName && eumYangAnalysisService.isEumYangUnbalanced(nameBaleumEumyang)) {
                    return@mapNotNull null
                }

                val nameHoeksuOhaeng = OhaengCalculationUtils.calculateHoeksuListToOhaeng(hanjaHoeksuValues)

                // 오행 체크
                if (!multiOhaengHarmonyAnalyzer.checkHoeksuOhaengHarmony(nameHoeksuOhaeng, isComplexSurnameSingleName)) {
                    return@mapNotNull null
                }

                val sagyeokSuriOhaeng = OhaengCalculationUtils.calculateHoeksuListToOhaeng(sagyeok.getValues())

                // 사격 오행 체크
                if (!multiOhaengHarmonyAnalyzer.checkSagyeokSuriOhaengHarmony(sagyeokSuriOhaeng, isComplexSurnameSingleName)) {
                    return@mapNotNull null
                }

                val minScore = if (requireMinScore) {
                    NamingCalculationUtils.getMinScore(isComplexSurnameSingleName, surLength, nameLength)
                } else {
                    0
                }

                if (score >= minScore) {
                    GoodCombination(
                        nameHanjaHoeksu = hanjaHoeksuValues,
                        sagyeok = sagyeok,
                        nameBaleumEumyang = nameBaleumEumyang,
                        nameHoeksuOhaeng = nameHoeksuOhaeng,
                        sagyeokSuriOhaeng = sagyeokSuriOhaeng
                    )
                } else null
            }
        } catch (e: Exception) {
            throw NamingException.HanjaException(
                "이름 조합 분석 실패",
                hanja = surHanja,
                cause = e
            )
        }
    }
}