// model/common/utils/ChosungUtils.kt
package com.ssc.namespring.model.common.utils

object ChosungUtils {
    private val CHOSUNG_LIST = arrayOf(
        "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ",
        "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    )

    fun getChosung(text: String): String {
        if (text.isEmpty()) return ""
        val char = text[0]
        val code = char.code - 0xAC00
        if (code < 0 || code > 11171) return ""

        val index = code / 588
        return if (index in CHOSUNG_LIST.indices) {
            CHOSUNG_LIST[index]
        } else {
            ""
        }
    }

    fun extractChosung(text: String): String {
        return text.map { char ->
            getChosung(char.toString())
        }.joinToString("")
    }

    fun matchesChosungPattern(text: String, pattern: String): Boolean {
        val textChosung = extractChosung(text)
        return textChosung.contains(pattern)
    }
}