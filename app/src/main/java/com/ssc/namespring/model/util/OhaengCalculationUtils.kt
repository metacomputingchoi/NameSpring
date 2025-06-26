// model/util/OhaengCalculationUtils.kt
package com.ssc.namespring.model.util

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.common.saju.SajuConstants

object OhaengCalculationUtils {

    fun calculateHoeksuToOhaeng(hoeksu: Int): Int {
        val ne = (hoeksu % NamingCalculationConstants.STROKE_MODULO) +
                (hoeksu % NamingCalculationConstants.STROKE_MODULO) % NamingCalculationConstants.YIN_YANG_MODULO
        return if (ne == NamingCalculationConstants.STROKE_MODULO) 0 else ne
    }

    fun calculateHoeksuListToOhaeng(hoeksuList: List<Int>): List<Int> {
        return hoeksuList.map { calculateHoeksuToOhaeng(it) }
    }

    fun calculateOhaengDifference(prevElement: Int, currElement: Int): Int {
        return (currElement - prevElement + NamingCalculationConstants.OHAENG_COUNT) %
                NamingCalculationConstants.OHAENG_COUNT
    }

    fun calculateOhaengDifferenceByString(prevElement: String, currElement: String): Int {
        val prevIndex = SajuConstants.OHAENG_SUNSE.indexOf(prevElement)
        val currIndex = SajuConstants.OHAENG_SUNSE.indexOf(currElement)
        return calculateOhaengDifference(prevIndex, currIndex)
    }

    fun getOhaengRelation(difference: Int): OhaengRelation {
        return when (difference) {
            SajuConstants.Relations.GENERATING_FORWARD,
            SajuConstants.Relations.GENERATING_BACKWARD -> OhaengRelation.GENERATING
            SajuConstants.Relations.CONFLICTING_FORWARD,
            SajuConstants.Relations.CONFLICTING_BACKWARD -> OhaengRelation.CONFLICTING
            else -> OhaengRelation.NEUTRAL
        }
    }

    enum class OhaengRelation {
        GENERATING,  // 상생
        CONFLICTING, // 상극
        NEUTRAL      // 중립
    }
}