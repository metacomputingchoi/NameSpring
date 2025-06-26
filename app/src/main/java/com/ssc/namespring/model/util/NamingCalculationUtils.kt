// model/util/NamingCalculationUtils.kt
package com.ssc.namespring.model.util

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.Sagyeok

object NamingCalculationUtils {

    fun calculateSagyeok(hanjaHoeksuValues: List<Int>, surLength: Int): Sagyeok {
        val hyeong = hanjaHoeksuValues.subList(surLength, hanjaHoeksuValues.size).sum()
        val won = hanjaHoeksuValues[surLength - 1] + hanjaHoeksuValues[surLength]
        val i = hanjaHoeksuValues.first() + hanjaHoeksuValues.last()
        val jeong = hanjaHoeksuValues.sum() % NamingCalculationConstants.JEONG_MODULO

        return Sagyeok(hyeong, won, i, jeong)
    }

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

    fun isComplexSurnameSingleName(surLength: Int, nameLength: Int): Boolean {
        return surLength >= 2 && nameLength == 1
    }

    fun countGilhanHoeksu(values: List<Int>): Int {
        return values.count { it in NamingCalculationConstants.GILHAN_HOEKSU }
    }

    // 오행 계산 관련 메소드들은 OhaengCalculationUtils로 이동
}