// model/common/constants/ScoringConstants.kt
package com.ssc.namespring.model.common.constants

object ScoringConstants {
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
    const val YIN_YANG_SET_SIZE = 2
    const val PM_FIRST_INDEX = 0
    const val PM_THIRD_INDEX = 2
}