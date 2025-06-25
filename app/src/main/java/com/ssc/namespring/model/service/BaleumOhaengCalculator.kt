// model/service/BaleumOhaengCalculator.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.Constants
import com.ssc.namespring.model.util.toHangulDecomposition

class BaleumOhaengCalculator(private val cacheManager: CacheManager) {

    fun getBaleumOhaeng(char: Char): String? {
        return cacheManager.baleumOhaengCache.getOrPut(char) {
            val (cho, _, _) = char.toHangulDecomposition()
            Constants.INITIALS.getOrNull(cho)?.let { initial ->
                Constants.CHOSUNG_BALEUM_OHAENG[initial]
            }
        }
    }

    fun getBaleumEumyang(char: Char): Int? {
        return cacheManager.baleumEumyangCache.getOrPut(char) {
            val (_, jung, _) = char.toHangulDecomposition()
            Constants.MEDIALS.getOrNull(jung)?.let { medial ->
                when (medial) {
                    in Constants.EUM_JUNGSEONG -> 0
                    in Constants.YANG_JUNGSEONG -> 1
                    else -> null
                }
            }
        }
    }
}
