// model/service/NameSuriAnalyzer.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.Constants
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
            repeat(nameLength) { add((Constants.MIN_STROKE..Constants.MAX_STROKE).toList()) }
        }

        val isComplexSurnameSingleName = surLength >= 2 && nameLength == 1

        val indexRanges: List<List<Int>> = hoeksuRanges.map { range ->
            range.indices.toList()
        }

        return cartesianProduct(indexRanges).mapNotNull { indices ->
            val hanjaHoeksuValues = indices.mapIndexed { i, idx -> hoeksuRanges[i][idx] }

            val sagyeok = calculateSagyeok(hanjaHoeksuValues, surLength)
            val score = sagyeok.getValues().count { it in Constants.GILHAN_HOEKSU }

            val nameBaleumEumyang = hanjaHoeksuValues.map { it % Constants.YIN_YANG_MODULO }

            // 음양 체크
            if (!isComplexSurnameSingleName &&
                (nameBaleumEumyang.sum() == 0 || nameBaleumEumyang.sum() == nameBaleumEumyang.size)) {
                return@mapNotNull null
            }

            val nameHoeksuOhaeng = hanjaHoeksuValues.map { sv ->
                val ne = (sv % Constants.STROKE_MODULO) +
                        (sv % Constants.STROKE_MODULO) % Constants.YIN_YANG_MODULO
                if (ne == Constants.STROKE_MODULO) 0 else ne
            }

            // 오행 체크
            if (!multiOhaengHarmonyAnalyzer.checkHoeksuOhaengHarmony(nameHoeksuOhaeng, isComplexSurnameSingleName)) {
                return@mapNotNull null
            }

            val sagyeokSuriOhaeng = sagyeok.getValues().map { ft ->
                val te = (ft % Constants.STROKE_MODULO) +
                        (ft % Constants.STROKE_MODULO) % Constants.YIN_YANG_MODULO
                if (te == Constants.STROKE_MODULO) 0 else te
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
        val jeong = hanjaHoeksuValues.sum() % Constants.JEONG_MODULO

        return Sagyeok(hyeong, won, i, jeong)
    }

    private fun getMinScore(
        isComplexSurnameSingleName: Boolean,
        surLength: Int,
        nameLength: Int
    ): Int {
        return when {
            isComplexSurnameSingleName -> Constants.MinScore.COMPLEX_SURNAME_SINGLE_NAME
            surLength == 1 && nameLength == 1 -> Constants.MinScore.SINGLE_SURNAME_SINGLE_NAME
            else -> Constants.MinScore.DEFAULT
        }
    }
}
