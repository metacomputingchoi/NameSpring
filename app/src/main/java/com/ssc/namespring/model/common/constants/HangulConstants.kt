// model/common/constants/HangulConstants.kt
package com.ssc.namespring.model.common.constants

object HangulConstants {
    val HANGUL_STROKES = mapOf(
        "" to 0, "ㄱ" to 2, "ㄲ" to 4, "ㄴ" to 2, "ㄷ" to 3, "ㄸ" to 6,
        "ㄹ" to 5, "ㅁ" to 4, "ㅂ" to 4, "ㅃ" to 8, "ㅅ" to 2, "ㅆ" to 4,
        "ㅇ" to 1, "ㅈ" to 3, "ㅉ" to 6, "ㅊ" to 4, "ㅋ" to 3, "ㅌ" to 4,
        "ㅍ" to 4, "ㅎ" to 3, "ㄳ" to 4, "ㄵ" to 5, "ㄶ" to 5, "ㄺ" to 7,
        "ㄻ" to 9, "ㄼ" to 9, "ㄽ" to 7, "ㄾ" to 9, "ㄿ" to 9, "ㅀ" to 8,
        "ㅄ" to 6, "ㅏ" to 2, "ㅐ" to 3, "ㅑ" to 3, "ㅒ" to 4, "ㅓ" to 2,
        "ㅔ" to 3, "ㅕ" to 3, "ㅖ" to 4, "ㅗ" to 2, "ㅘ" to 4, "ㅙ" to 5,
        "ㅚ" to 3, "ㅛ" to 3, "ㅜ" to 2, "ㅝ" to 4, "ㅞ" to 5, "ㅟ" to 3,
        "ㅠ" to 3, "ㅡ" to 1, "ㅢ" to 2, "ㅣ" to 1
    )

    val HANGUL_CHO_LIST = listOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
    val HANGUL_JUNG_LIST = listOf('ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ')
    val HANGUL_JONG_LIST = listOf("", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ")
    val YANG_VOWELS = listOf('ㅏ', 'ㅑ', 'ㅐ', 'ㅒ', 'ㅗ', 'ㅛ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅣ')

    const val HANGUL_BASE = '가'
    const val HANGUL_CHO_DIVISOR = 588
    const val HANGUL_JUNG_DIVISOR = 28
    const val HANGUL_JUNG_COUNT = 21
    const val HANGUL_CODE_OFFSET = 44032
}