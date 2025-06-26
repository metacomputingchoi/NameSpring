// model/util/NamingCalculationUtils.kt
package com.ssc.namespring.model.util

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.Sagyeok

object NamingCalculationUtils {

    /**
     * 사격(四格) 계산
     */
    fun calculateSagyeok(hanjaHoeksuValues: List<Int>, surLength: Int): Sagyeok {
        val hyeong = hanjaHoeksuValues.subList(surLength, hanjaHoeksuValues.size).sum()
        val won = hanjaHoeksuValues[surLength - 1] + hanjaHoeksuValues[surLength]
        val i = hanjaHoeksuValues.first() + hanjaHoeksuValues.last()
        val jeong = hanjaHoeksuValues.sum() % NamingCalculationConstants.JEONG_MODULO

        return Sagyeok(hyeong, won, i, jeong)
    }

    /**
     * 최소 점수 계산
     */
    fun getMinScore(
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

    /**
     * 획수를 오행으로 변환
     */
    fun calculateHoeksuToOhaeng(hoeksu: Int): Int {
        val ne = (hoeksu % NamingCalculationConstants.STROKE_MODULO) +
                (hoeksu % NamingCalculationConstants.STROKE_MODULO) % NamingCalculationConstants.YIN_YANG_MODULO
        return if (ne == NamingCalculationConstants.STROKE_MODULO) 0 else ne
    }

    /**
     * 획수 리스트를 오행 리스트로 변환
     */
    fun calculateHoeksuListToOhaeng(hoeksuList: List<Int>): List<Int> {
        return hoeksuList.map { calculateHoeksuToOhaeng(it) }
    }

    /**
     * 음양 균형 체크
     */
    fun isYinYangUnbalanced(eumyangList: List<Int>): Boolean {
        return eumyangList.sum() == 0 || eumyangList.sum() == eumyangList.size
    }

    /**
     * 복성단명 여부 체크
     */
    fun isComplexSurnameSingleName(surLength: Int, nameLength: Int): Boolean {
        return surLength >= 2 && nameLength == 1
    }
}