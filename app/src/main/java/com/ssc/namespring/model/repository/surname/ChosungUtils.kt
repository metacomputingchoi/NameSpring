// model/repository/surname/ChosungUtils.kt
package com.ssc.namespring.model.repository.surname

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
        return CHOSUNG_LIST[code / 588]
    }
}