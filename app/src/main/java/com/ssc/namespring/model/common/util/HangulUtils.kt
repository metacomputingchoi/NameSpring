// model/common/util/HangulUtils.kt
package com.ssc.namespring.model.common.util

import com.ssc.namespring.model.common.constants.HangulConstants
import com.ssc.namespring.model.common.constants.ElementConstants

object HangulUtils {

    fun getHangulElement(char: Char?): String? {
        if (char == null) return null
        val cho = (char.code - HangulConstants.HANGUL_BASE.code) / HangulConstants.HANGUL_CHO_DIVISOR
        return ElementConstants.FIVE_ELEMENTS[HangulConstants.HANGUL_CHO_LIST[cho]]
    }

    fun getHangulPn(char: Char): Int {
        val jung = (char.code - HangulConstants.HANGUL_BASE.code) / HangulConstants.HANGUL_JUNG_DIVISOR % HangulConstants.HANGUL_JUNG_COUNT
        return if (HangulConstants.HANGUL_JUNG_LIST[jung] in HangulConstants.YANG_VOWELS) 1 else 0
    }

    fun getHangulStrokeCount(char: Char): Int {
        val charCode = char.code - HangulConstants.HANGUL_CODE_OFFSET
        val finaleOffset = charCode % HangulConstants.HANGUL_JUNG_DIVISOR
        val medialOffset = (charCode / HangulConstants.HANGUL_JUNG_DIVISOR) % HangulConstants.HANGUL_JUNG_COUNT
        val initialOffset = charCode / (HangulConstants.HANGUL_JUNG_DIVISOR * HangulConstants.HANGUL_JUNG_COUNT)

        return (HangulConstants.HANGUL_STROKES[HangulConstants.HANGUL_CHO_LIST[initialOffset].toString()] ?: 0) +
                (HangulConstants.HANGUL_STROKES[HangulConstants.HANGUL_JUNG_LIST[medialOffset].toString()] ?: 0) +
                (HangulConstants.HANGUL_STROKES[HangulConstants.HANGUL_JONG_LIST[finaleOffset]] ?: 0)
    }
}