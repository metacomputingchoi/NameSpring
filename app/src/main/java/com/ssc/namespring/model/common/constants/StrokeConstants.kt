// model/common/constants/StrokeConstants.kt
package com.ssc.namespring.model.common.constants

object StrokeConstants {
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