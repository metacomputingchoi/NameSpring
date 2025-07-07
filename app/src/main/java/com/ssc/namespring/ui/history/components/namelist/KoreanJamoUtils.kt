// ui/history/components/namelist/KoreanJamoUtils.kt
package com.ssc.namespring.ui.history.components.namelist

object KoreanJamoUtils {

    private val CHOSUNG = charArrayOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
        'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    private val JUNGSUNG = charArrayOf(
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
        'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    )

    private val JONGSUNG = charArrayOf(
        '\u0000', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
        'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    fun extractChosung(text: String): String {
        return text.map { getChosungAt(it) }.joinToString("")
    }

    fun getChosungAt(char: Char): Char {
        val code = char.code
        return if (code in 0xAC00..0xD7A3) {
            val index = (code - 0xAC00) / (21 * 28)
            CHOSUNG[index]
        } else char
    }

    fun isChosung(char: Char): Boolean = char in CHOSUNG
    fun isJungsung(char: Char): Boolean = char in JUNGSUNG
    fun isJongsung(char: Char): Boolean = char in JONGSUNG && char != '\u0000'

    fun containsJamo(char: Char, jamo: Char): Boolean {
        if (char.code !in 0xAC00..0xD7A3) return false

        val code = char.code - 0xAC00
        val jong = code % 28
        val jung = (code / 28) % 21
        val cho = code / (21 * 28)

        return when {
            isChosung(jamo) -> CHOSUNG[cho] == jamo
            isJungsung(jamo) -> JUNGSUNG[jung] == jamo
            isJongsung(jamo) -> jong > 0 && JONGSUNG[jong] == jamo
            else -> false
        }
    }

    fun tryAssembleJamo(text: String): String {
        val chars = text.toCharArray()
        val result = StringBuilder()
        var i = 0

        while (i < chars.size) {
            val cho = chars.getOrNull(i)
            val jung = chars.getOrNull(i + 1)
            val jong = chars.getOrNull(i + 2)

            if (cho != null && isChosung(cho) && jung != null && isJungsung(jung)) {
                val choIdx = CHOSUNG.indexOf(cho)
                val jungIdx = JUNGSUNG.indexOf(jung)
                var jongIdx = 0
                var consumed = 2

                if (jong != null && isJongsung(jong)) {
                    jongIdx = JONGSUNG.indexOf(jong)
                    consumed = 3
                }

                if (choIdx >= 0 && jungIdx >= 0) {
                    val unicode = 0xAC00 + choIdx * 21 * 28 + jungIdx * 28 + jongIdx
                    result.append(unicode.toChar())
                    i += consumed
                    continue
                }
            }

            result.append(chars[i])
            i++
        }

        return result.toString()
    }

    fun editDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }

        return dp[m][n]
    }
}