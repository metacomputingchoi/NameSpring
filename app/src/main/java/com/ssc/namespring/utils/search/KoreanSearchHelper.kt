// utils/search/KoreanSearchHelper.kt
package com.ssc.namespring.utils.search

class KoreanSearchHelper {

    private val CHOSUNG_LIST = arrayOf(
        "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ",
        "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    )

    fun matches(text: String, query: String): Boolean {
        if (query.isEmpty()) return true
        if (text.isEmpty()) return false

        // 1. 완전 일치
        if (text.contains(query, ignoreCase = true)) return true

        // 2. 초성 검색
        val chosungText = getChosung(text)
        if (chosungText.contains(query)) return true

        // 3. 혼합 검색 (초성 + 완성형)
        return matchesMixed(text, query)
    }

    private fun getChosung(text: String): String {
        return text.map { char ->
            if (char in '가'..'힣') {
                val code = char.code - 0xAC00
                val chosungIndex = code / 588
                CHOSUNG_LIST[chosungIndex]
            } else {
                char.toString()
            }
        }.joinToString("")
    }

    private fun matchesMixed(text: String, query: String): Boolean {
        var textIndex = 0
        var queryIndex = 0

        while (textIndex < text.length && queryIndex < query.length) {
            val textChar = text[textIndex]
            val queryChar = query[queryIndex]

            when {
                // 완전 일치
                textChar == queryChar -> {
                    textIndex++
                    queryIndex++
                }
                // 초성 일치
                isChosung(queryChar) && matchesChosung(textChar, queryChar) -> {
                    textIndex++
                    queryIndex++
                }
                else -> {
                    textIndex++
                }
            }
        }

        return queryIndex == query.length
    }

    private fun isChosung(char: Char): Boolean {
        return char.toString() in CHOSUNG_LIST
    }

    private fun matchesChosung(textChar: Char, chosungChar: Char): Boolean {
        if (textChar !in '가'..'힣') return false

        val code = textChar.code - 0xAC00
        val chosungIndex = code / 588
        val chosung = CHOSUNG_LIST[chosungIndex]

        return chosung == chosungChar.toString()
    }
}