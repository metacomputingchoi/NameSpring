// model/service/NameSuriAnalyzer.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.Sagyeok
import com.ssc.namespring.model.data.GoodCombination
import com.ssc.namespring.model.util.cartesianProduct

class NameSuriAnalyzer(
    private val hanjaHoeksuAnalyzer: HanjaHoeksuAnalyzer,
    private val multiOhaengHarmonyAnalyzer: MultiOhaengHarmonyAnalyzer
) {

    fun analyzeNameCombinations(
        surHangul: String,
        surHanja: String,
        nameLength: Int
    ): List<GoodCombination> {
        val surLength = surHanja.length
        val surHanjaHoeksu = surHanja.map {
            hanjaHoeksuAnalyzer.getHanjaHoeksu(it.toString()) ?: 0
        }

        val hoeksuRanges = buildList {
            addAll(surHanjaHoeksu.map { listOf(it) })
            repeat(nameLength) { add((NamingCalculationConstants.MIN_STROKE..NamingCalculationConstants.MAX_STROKE).toList()) }
        }

        val isComplexSurnameSingleName = surLength >= 2 && nameLength == 1

        val indexRanges: List<List<Int>> = hoeksuRanges.map { range ->
            range.indices.toList()
        }

        return cartesianProduct(indexRanges).mapNotNull { indices ->
            val hanjaHoeksuValues = indices.mapIndexed { i, idx -> hoeksuRanges[i][idx] }

            val sagyeok = calculateSagyeok(hanjaHoeksuValues, surLength)
            val score = sagyeok.getValues().count { it in NamingCalculationConstants.GILHAN_HOEKSU }

            val nameBaleumEumyang = hanjaHoeksuValues.map { it % NamingCalculationConstants.YIN_YANG_MODULO }

            // 음양 체크
            if (!isComplexSurnameSingleName &&
                (nameBaleumEumyang.sum() == 0 || nameBaleumEumyang.sum() == nameBaleumEumyang.size)) {
                return@mapNotNull null
            }

            val nameHoeksuOhaeng = hanjaHoeksuValues.map { sv ->
                val ne = (sv % NamingCalculationConstants.STROKE_MODULO) +
                        (sv % NamingCalculationConstants.STROKE_MODULO) % NamingCalculationConstants.YIN_YANG_MODULO
                if (ne == NamingCalculationConstants.STROKE_MODULO) 0 else ne
            }

            // 오행 체크
            if (!multiOhaengHarmonyAnalyzer.checkHoeksuOhaengHarmony(nameHoeksuOhaeng, isComplexSurnameSingleName)) {
                return@mapNotNull null
            }

            val sagyeokSuriOhaeng = sagyeok.getValues().map { ft ->
                val te = (ft % NamingCalculationConstants.STROKE_MODULO) +
                        (ft % NamingCalculationConstants.STROKE_MODULO) % NamingCalculationConstants.YIN_YANG_MODULO
                if (te == NamingCalculationConstants.STROKE_MODULO) 0 else te
            }

            // 사격 오행 체크
            if (!multiOhaengHarmonyAnalyzer.checkSagyeokSuriOhaengHarmony(sagyeokSuriOhaeng, isComplexSurnameSingleName)) {
                return@mapNotNull null
            }

            val minScore = getMinScore(isComplexSurnameSingleName, surLength, nameLength)

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
    }

    private fun calculateSagyeok(hanjaHoeksuValues: List<Int>, surLength: Int): Sagyeok {
        val hyeong = hanjaHoeksuValues.subList(surLength, hanjaHoeksuValues.size).sum()
        val won = hanjaHoeksuValues[surLength - 1] + hanjaHoeksuValues[surLength]
        val i = hanjaHoeksuValues.first() + hanjaHoeksuValues.last()
        val jeong = hanjaHoeksuValues.sum() % NamingCalculationConstants.JEONG_MODULO

        return Sagyeok(hyeong, won, i, jeong)
    }

    private fun getMinScore(
        isComplexSurnameSingleName: Boolean,
        surLength: Int,
        nameLength: Int
    ): Int {
        return when {
            isComplexSurnameSingleName -> NamingCalculationConstants.MinScore.COMPLEX_SURNAME_SINGLE_NAME
            surLength == 1 && nameLength == 1 -> NamingCalculationConstants.MinScore.SINGLE_SURNAME_SINGLE_NAME
            else -> NamingCalculationConstants.MinScore.DEFAULT
        }
    }
}
