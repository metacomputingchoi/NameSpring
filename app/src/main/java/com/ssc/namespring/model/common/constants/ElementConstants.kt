// model/common/constants/ElementConstants.kt
package com.ssc.namespring.model.common.constants

object ElementConstants {
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
}