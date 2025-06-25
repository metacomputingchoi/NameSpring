// model/service/ElementCalculator.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.Constants
import com.ssc.namespring.model.util.toHangulDecomposition

class ElementCalculator(private val cacheManager: CacheManager) {

    fun getHangulElement(char: Char): String? {
        return cacheManager.hangulElementCache.getOrPut(char) {
            val (cho, _, _) = char.toHangulDecomposition()
            Constants.INITIALS.getOrNull(cho)?.let { initial ->
                Constants.INITIAL_ELEMENTS[initial]
            }
        }
    }

    fun getHangulPn(char: Char): Int? {
        return cacheManager.hangulPnCache.getOrPut(char) {
            val (_, jung, _) = char.toHangulDecomposition()
            Constants.MEDIALS.getOrNull(jung)?.let { medial ->
                when (medial) {
                    in Constants.YIN_MEDIALS -> 0
                    in Constants.YANG_MEDIALS -> 1
                    else -> null
                }
            }
        }
    }
}