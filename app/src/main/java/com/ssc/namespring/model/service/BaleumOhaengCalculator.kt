// model/service/BaleumOhaengCalculator.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.hangul.HangulConstants
import com.ssc.namespring.model.util.toHangulDecomposition

class BaleumOhaengCalculator(private val cacheManager: CacheManager) {

    companion object {
        // 초성 오행 매핑
        private val CHOSUNG_BALEUM_OHAENG = mapOf(
            'ㄱ' to "木", 'ㅋ' to "木",
            'ㄴ' to "火", 'ㄷ' to "火", 'ㄹ' to "火", 'ㅌ' to "火",
            'ㅇ' to "土", 'ㅎ' to "土",
            'ㅅ' to "金", 'ㅈ' to "金", 'ㅊ' to "金",
            'ㅁ' to "水", 'ㅂ' to "水", 'ㅍ' to "水"
        )

        // 중성 음양 분류
        private val EUM_JUNGSEONG = setOf('ㅓ', 'ㅕ', 'ㅔ', 'ㅖ', 'ㅜ', 'ㅠ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅢ', 'ㅡ')
        private val YANG_JUNGSEONG = setOf('ㅏ', 'ㅑ', 'ㅐ', 'ㅒ', 'ㅗ', 'ㅛ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅣ')
    }

    fun getBaleumOhaeng(char: Char): String? {
        return cacheManager.baleumOhaengCache.getOrPut(char) {
            val (cho, _, _) = char.toHangulDecomposition()
            HangulConstants.INITIALS.getOrNull(cho)?.let { initial ->
                CHOSUNG_BALEUM_OHAENG[initial]
            }
        }
    }

    fun getBaleumEumyang(char: Char): Int? {
        return cacheManager.baleumEumyangCache.getOrPut(char) {
            val (_, jung, _) = char.toHangulDecomposition()
            HangulConstants.MEDIALS.getOrNull(jung)?.let { medial ->
                when (medial) {
                    in EUM_JUNGSEONG -> 0
                    in YANG_JUNGSEONG -> 1
                    else -> null
                }
            }
        }
    }
}
