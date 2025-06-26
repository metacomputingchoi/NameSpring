// model/util/HangulUtils.kt
package com.ssc.namespring.model.util

import com.ssc.namespring.model.common.hangul.HangulConstants

object HangulUtils {

    /**
     * 한글 문자에서 초성 추출
     */
    fun getInitialFromHangul(char: Char): Char? {
        return if (char in HangulConstants.HANGUL_START..HangulConstants.HANGUL_END) {
            val (cho, _, _) = char.toHangulDecomposition()
            HangulConstants.INITIALS[cho]
        } else null
    }

    /**
     * 한글 문자열에서 모든 초성 추출
     */
    fun extractInitials(text: String): String {
        return text.mapNotNull { getInitialFromHangul(it) }.joinToString("")
    }
}