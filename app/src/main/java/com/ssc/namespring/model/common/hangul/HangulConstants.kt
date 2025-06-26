// model/common/hangul/HangulConstants.kt
package com.ssc.namespring.model.common.hangul

object HangulConstants {
    // 한글 자모
    val INITIALS = arrayOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
    val MEDIALS = arrayOf('ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ')
    val FINALES = arrayOf("", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ")

    // 획수 정의
    val STROKES = buildMap {
        // 초성 획수
        val initialStrokes = mapOf(
            'ㄱ' to 2, 'ㄲ' to 4, 'ㄴ' to 2, 'ㄷ' to 3, 'ㄸ' to 6,
            'ㄹ' to 5, 'ㅁ' to 4, 'ㅂ' to 4, 'ㅃ' to 8, 'ㅅ' to 2,
            'ㅆ' to 4, 'ㅇ' to 1, 'ㅈ' to 3, 'ㅉ' to 6, 'ㅊ' to 4,
            'ㅋ' to 3, 'ㅌ' to 4, 'ㅍ' to 4, 'ㅎ' to 3
        )

        // 중성 획수
        val medialStrokes = mapOf(
            'ㅏ' to 2, 'ㅐ' to 3, 'ㅑ' to 3, 'ㅒ' to 4, 'ㅓ' to 2,
            'ㅔ' to 3, 'ㅕ' to 3, 'ㅖ' to 4, 'ㅗ' to 2, 'ㅘ' to 4,
            'ㅙ' to 5, 'ㅚ' to 3, 'ㅛ' to 3, 'ㅜ' to 2, 'ㅝ' to 4,
            'ㅞ' to 5, 'ㅟ' to 3, 'ㅠ' to 3, 'ㅡ' to 1, 'ㅢ' to 2, 'ㅣ' to 1
        )

        // 종성 획수
        val finalStrokes = mapOf(
            "" to 0, "ㄱ" to 2, "ㄲ" to 4, "ㄳ" to 4, "ㄴ" to 2,
            "ㄵ" to 5, "ㄶ" to 5, "ㄷ" to 3, "ㄹ" to 5, "ㄺ" to 7,
            "ㄻ" to 9, "ㄼ" to 9, "ㄽ" to 7, "ㄾ" to 9, "ㄿ" to 9,
            "ㅀ" to 8, "ㅁ" to 4, "ㅂ" to 4, "ㅄ" to 6, "ㅅ" to 2,
            "ㅆ" to 4, "ㅇ" to 1, "ㅈ" to 3, "ㅊ" to 4, "ㅋ" to 3,
            "ㅌ" to 4, "ㅍ" to 4, "ㅎ" to 3
        )

        // 통합
        putAll(initialStrokes.mapKeys { it.key.toString() })
        putAll(medialStrokes.mapKeys { it.key.toString() })
        putAll(finalStrokes)
    }

    // 한글 유니코드 상수
    const val HANGUL_BASE = 0xAC00
    const val INITIAL_COUNT = 588
    const val MEDIAL_COUNT = 28
    const val MEDIALS_PER_INITIAL = 21

    // 한글 범위
    const val HANGUL_START = '가'
    const val HANGUL_END = '힣'
}
