// model/util/HangulUtils.kt
package com.ssc.namespring.model.util

import com.ssc.namespring.model.common.hangul.HangulConstants

object HangulUtils {

    fun getInitialFromHangul(char: Char): Char? {
        return if (char in HangulConstants.HANGUL_START..HangulConstants.HANGUL_END) {
            val (cho, _, _) = char.toHangulDecomposition()
            HangulConstants.INITIALS[cho]
        } else null
    }

    fun extractInitials(text: String): String {
        return text.mapNotNull { getInitialFromHangul(it) }.joinToString("")
    }
}