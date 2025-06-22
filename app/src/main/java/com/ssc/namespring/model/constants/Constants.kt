// model/constants/Constants.kt
package com.ssc.namespring.model.constants

object Constants {
    val STEM_ELEMENTS = mapOf(
        "甲" to "木", "乙" to "木", "丙" to "火", "丁" to "火",
        "戊" to "土", "己" to "土", "庚" to "金", "辛" to "金",
        "壬" to "水", "癸" to "水"
    )

    val BRANCH_ELEMENTS = mapOf(
        "子" to "水", "丑" to "土", "寅" to "木", "卯" to "木",
        "辰" to "土", "巳" to "火", "午" to "火", "未" to "土",
        "申" to "金", "酉" to "金", "戌" to "土", "亥" to "水"
    )

    val SIJU = listOf(
        listOf("甲子", "丙子", "戊子", "庚子", "壬子"),
        listOf("乙丑", "丁丑", "己丑", "辛丑", "癸丑"),
        listOf("丙寅", "戊寅", "庚寅", "壬寅", "甲寅"),
        listOf("丁卯", "己卯", "辛卯", "癸卯", "乙卯"),
        listOf("戊辰", "庚辰", "壬辰", "甲辰", "丙辰"),
        listOf("己巳", "辛巳", "癸巳", "乙巳", "丁巳"),
        listOf("庚午", "壬午", "甲午", "丙午", "戊午"),
        listOf("辛未", "癸未", "乙未", "丁未", "己未"),
        listOf("壬申", "甲申", "丙申", "戊申", "庚申"),
        listOf("癸酉", "乙酉", "丁酉", "己酉", "辛酉"),
        listOf("甲戌", "丙戌", "戊戌", "庚戌", "壬戌"),
        listOf("乙亥", "丁亥", "己亥", "辛亥", "癸亥")
    )

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

    val GOOD_LUCK = listOf(
        1, 3, 5, 6, 7, 8, 11, 13, 15, 16, 17, 18, 21, 23, 24, 25,
        29, 31, 32, 33, 35, 37, 38, 39, 41, 45, 47, 48, 52, 57,
        61, 63, 65, 67, 68, 81
    )

    val SCORING_RULES = mapOf(
        "four_types_luck" to mapOf(
            "perfect" to 10, "three" to 7, "two" to 4, "one" to 1, "zero" to 0
        ),
        "name_element_harmony" to mapOf(
            "perfect" to 15, "good" to 10, "neutral" to 5, "conflict" to -10
        ),
        "type_element_harmony" to mapOf(
            "perfect" to 15, "good" to 10, "neutral" to 5, "conflict" to -10
        ),
        "yin_yang_balance" to mapOf(
            "perfect" to 10, "good" to 7, "poor" to 0
        ),
        "saju_complement" to mapOf(
            "perfect" to 20, "good" to 15, "neutral" to 10, "poor" to 5
        ),
        "pronunciation" to mapOf(
            "natural" to 10, "unnatural" to 0
        ),
        "meaning" to mapOf(
            "excellent" to 10, "good" to 7, "neutral" to 5
        )
    )

    val FIVE_ELEMENTS = mapOf(
        'ㄱ' to "木", 'ㅋ' to "木", 'ㄴ' to "火", 'ㄷ' to "火",
        'ㄹ' to "火", 'ㅌ' to "火", 'ㅇ' to "土", 'ㅎ' to "土",
        'ㅅ' to "金", 'ㅈ' to "金", 'ㅊ' to "金", 'ㅁ' to "水",
        'ㅂ' to "水", 'ㅍ' to "水"
    )
}
