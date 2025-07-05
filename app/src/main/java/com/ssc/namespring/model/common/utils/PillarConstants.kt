// model/common/utils/PillarConstants.kt
package com.ssc.namespring.model.common.utils

object PillarConstants {
    val HEAVENLY_STEM_OHAENG = mapOf(
        "甲" to "木", "乙" to "木",
        "丙" to "火", "丁" to "火",
        "戊" to "土", "己" to "土",
        "庚" to "金", "辛" to "金",
        "壬" to "水", "癸" to "水"
    )

    val EARTHLY_BRANCH_OHAENG = mapOf(
        "子" to "水", "丑" to "土", "寅" to "木", "卯" to "木",
        "辰" to "土", "巳" to "火", "午" to "火", "未" to "土",
        "申" to "金", "酉" to "金", "戌" to "土", "亥" to "水"
    )

    val HEAVENLY_STEM_EUMYANG = mapOf(
        "甲" to 1, "乙" to 0,
        "丙" to 1, "丁" to 0,
        "戊" to 1, "己" to 0,
        "庚" to 1, "辛" to 0,
        "壬" to 1, "癸" to 0
    )

    val EARTHLY_BRANCH_EUMYANG = mapOf(
        "子" to 1, "丑" to 0, "寅" to 1, "卯" to 0,
        "辰" to 1, "巳" to 0, "午" to 1, "未" to 0,
        "申" to 1, "酉" to 0, "戌" to 1, "亥" to 0
    )
}