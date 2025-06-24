// model/common/util/HangulUtils.kt
package com.ssc.namespring.model.common.util

import com.ssc.namespring.model.common.constants.Constants

object HangulUtils {

    fun getHangulElement(char: Char?): String? {
        if (char == null) return null
        val cho = (char.code - Constants.HANGUL_BASE.code) / Constants.HANGUL_CHO_DIVISOR
        return Constants.FIVE_ELEMENTS[Constants.HANGUL_CHO_LIST[cho]]
    }

    fun getHangulPn(char: Char): Int {
        val jung = (char.code - Constants.HANGUL_BASE.code) / Constants.HANGUL_JUNG_DIVISOR % Constants.HANGUL_JUNG_COUNT
        return if (Constants.HANGUL_JUNG_LIST[jung] in Constants.YANG_VOWELS) 1 else 0
    }

    fun getHangulStrokeCount(char: Char): Int {
        val charCode = char.code - Constants.HANGUL_CODE_OFFSET
        val finaleOffset = charCode % Constants.HANGUL_JUNG_DIVISOR
        val medialOffset = (charCode / Constants.HANGUL_JUNG_DIVISOR) % Constants.HANGUL_JUNG_COUNT
        val initialOffset = charCode / (Constants.HANGUL_JUNG_DIVISOR * Constants.HANGUL_JUNG_COUNT)

        return (Constants.HANGUL_STROKES[Constants.HANGUL_CHO_LIST[initialOffset].toString()] ?: 0) +
                (Constants.HANGUL_STROKES[Constants.HANGUL_JUNG_LIST[medialOffset].toString()] ?: 0) +
                (Constants.HANGUL_STROKES[Constants.HANGUL_JONG_LIST[finaleOffset]] ?: 0)
    }
}