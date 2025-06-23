// model/common/constants/SajuConstants.kt
package com.ssc.namespring.model.common.constants

object SajuConstants {
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
        "23:30:00" to "01:30:00",
        "01:30:00" to "03:30:00",
        "03:30:00" to "05:30:00",
        "05:30:00" to "07:30:00",
        "07:30:00" to "09:30:00",
        "09:30:00" to "11:30:00",
        "11:30:00" to "13:30:00",
        "13:30:00" to "15:30:00",
        "15:30:00" to "17:30:00",
        "17:30:00" to "19:30:00",
        "19:30:00" to "21:30:00",
        "21:30:00" to "23:30:00"
    )
}