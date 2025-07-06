// model/domain/service/search/hanja/HanjaSearchUtils.kt
package com.ssc.namespring.model.domain.service.search.hanja

internal object HanjaSearchUtils {

    fun containsHanja(text: String): Boolean {
        return text.any { char ->
            val code = char.code
            // CJK Unified Ideographs 범위
            code in 0x4E00..0x9FFF ||
                    code in 0x3400..0x4DBF ||
                    code in 0x20000..0x2A6DF ||
                    code in 0x2A700..0x2B73F ||
                    code in 0x2B740..0x2B81F ||
                    code in 0x2B820..0x2CEAF ||
                    code in 0xF900..0xFAFF ||
                    code in 0x2F800..0x2FA1F
        }
    }

    fun getChosung(char: Char): String {
        val code = char.code - 0xAC00
        if (code < 0 || code > 11171) return ""

        val chosungList = arrayOf(
            "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ",
            "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
        )

        val index = code / 588
        return if (index in chosungList.indices) chosungList[index] else ""
    }
}