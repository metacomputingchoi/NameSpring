// model/common/util/constants/Constants.kt
package com.ssc.namespring.model.common.constants

object Constants {
    // Analysis
    const val DEFAULT_BIRTH_YEAR = 2025
    const val DEFAULT_BIRTH_MONTH = 6
    const val DEFAULT_BIRTH_DAY = 11
    const val DEFAULT_BIRTH_HOUR = 14
    const val DEFAULT_BIRTH_MINUTE = 30
    const val SEPARATOR_LINE_LENGTH = 60
    const val TOP_RESULTS_COUNT = 3

    // Element
    val ELEMENTS = listOf("木", "火", "土", "金", "水")
    val FIVE_ELEMENTS = mapOf(
        'ㄱ' to "木", 'ㅋ' to "木", 'ㄴ' to "火", 'ㄷ' to "火",
        'ㄹ' to "火", 'ㅌ' to "火", 'ㅇ' to "土", 'ㅎ' to "土",
        'ㅅ' to "金", 'ㅈ' to "金", 'ㅊ' to "金", 'ㅁ' to "水",
        'ㅂ' to "水", 'ㅍ' to "水"
    )
    const val ELEMENT_COUNT = 5
    const val ELEMENT_HARMONY_DIFF_1 = 1
    const val ELEMENT_HARMONY_DIFF_2 = 4
    const val ELEMENT_CONFLICT_DIFF_1 = 2
    const val ELEMENT_CONFLICT_DIFF_2 = 3

    // Filter
    const val NAME_LENGTH = 2
    const val COMBINED_LENGTH = 3
    const val MIN_PM_DIVERSITY = 1
    const val PM_FIRST_INDEX = 0
    const val PM_THIRD_INDEX = 2

    // Hangul
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

    // Saju
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
    const val LATE_NIGHT_HOUR = 23
    const val LATE_NIGHT_MINUTE = 30
    const val COL_INDEX_ADD = 1
    const val COL_INDEX_MODULO = 5
    val TIME_RANGES = listOf(
        "23:30:00" to "01:30:00", "01:30:00" to "03:30:00",
        "03:30:00" to "05:30:00", "05:30:00" to "07:30:00",
        "07:30:00" to "09:30:00", "09:30:00" to "11:30:00",
        "11:30:00" to "13:30:00", "13:30:00" to "15:30:00",
        "15:30:00" to "17:30:00", "17:30:00" to "19:30:00",
        "19:30:00" to "21:30:00", "21:30:00" to "23:30:00"
    )

    // Scoring
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
    const val SCORE_LUCK_PERFECT = 4
    const val SCORE_LUCK_THREE = 3
    const val SCORE_LUCK_TWO = 2
    const val SCORE_LUCK_ONE = 1
    const val SCORE_NAME_COEXIST_PERFECT = 2
    const val SCORE_NAME_COEXIST_GOOD = 1
    const val SCORE_TYPE_COEXIST_PERFECT = 3
    const val SCORE_TYPE_COEXIST_GOOD = 2
    const val SCORE_TYPE_COEXIST_NEUTRAL = 1
    const val SCORE_YIN_YANG_SET_SIZE = 2

    // Stroke
    val GOOD_LUCK = listOf(
        1, 3, 5, 6, 7, 8, 11, 13, 15, 16, 17, 18, 21, 23, 24, 25,
        29, 31, 32, 33, 35, 37, 38, 39, 41, 45, 47, 48, 52, 57,
        61, 63, 65, 67, 68, 81
    )
    const val MIN_STROKE = 1
    const val MAX_STROKE = 27
    const val LUCK_ARRAY_SIZE = 82
    const val MODULO_VALUE = 81
    const val NAME_PN_SUM_INVALID_ZERO = 0
    const val NAME_PN_SUM_INVALID_THREE = 3
    const val ELEMENT_NORMALIZE_VALUE = 10
    const val ELEMENT_LOOP_START = 1
    const val ELEMENT_LOOP_END = 2
    const val ELEMENT_DIFF_CONFLICT_1 = 4
    const val ELEMENT_DIFF_CONFLICT_2 = -6
    const val ELEMENT_DIFF_HARMONY_1 = 2
    const val ELEMENT_DIFF_HARMONY_2 = -8
    const val MIN_FINAL_SCORE = 4
    const val TYPE_ELEMENT_LOOP_START = 1
    const val TYPE_ELEMENT_LOOP_END = 3
}