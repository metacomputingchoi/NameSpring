// model/common/saju/SajuConstants.kt
package com.ssc.namespring.model.common.saju

// 피드백: Constants를 카테고리별로 분리하여 찾기 쉽게 만듦
object SajuConstants {
    // 천간 오행 매핑
    val CHEONGAN_OHAENG = mapOf(
        "甲" to "木", "乙" to "木",
        "丙" to "火", "丁" to "火",
        "戊" to "土", "己" to "土",
        "庚" to "金", "辛" to "金",
        "壬" to "水", "癸" to "水"
    )

    // 지지 오행 매핑
    val JIJI_OHAENG = mapOf(
        "子" to "水", "丑" to "土", "寅" to "木", "卯" to "木",
        "辰" to "土", "巳" to "火", "午" to "火", "未" to "土",
        "申" to "金", "酉" to "金", "戌" to "土", "亥" to "水"
    )

    // 시주 배열
    val SIJU = arrayOf(
        arrayOf("甲子", "丙子", "戊子", "庚子", "壬子"),
        arrayOf("乙丑", "丁丑", "己丑", "辛丑", "癸丑"),
        arrayOf("丙寅", "戊寅", "庚寅", "壬寅", "甲寅"),
        arrayOf("丁卯", "己卯", "辛卯", "癸卯", "乙卯"),
        arrayOf("戊辰", "庚辰", "壬辰", "甲辰", "丙辰"),
        arrayOf("己巳", "辛巳", "癸巳", "乙巳", "丁巳"),
        arrayOf("庚午", "壬午", "甲午", "丙午", "戊午"),
        arrayOf("辛未", "癸未", "乙未", "丁未", "己未"),
        arrayOf("壬申", "甲申", "丙申", "戊申", "庚申"),
        arrayOf("癸酉", "乙酉", "丁酉", "己酉", "辛酉"),
        arrayOf("甲戌", "丙戌", "戊戌", "庚戌", "壬戌"),
        arrayOf("乙亥", "丁亥", "己亥", "辛亥", "癸亥")
    )

    // 오행 순서
    val OHAENG_SUNSE = listOf("木", "火", "土", "金", "水")

    // 천간 그룹
    object StemGroups {
        val WOOD_STEMS = setOf('甲', '乙')
        val FIRE_STEMS = setOf('丙', '丁')
        val EARTH_STEMS = setOf('戊', '己')
        val METAL_STEMS = setOf('庚', '辛')
        val WATER_STEMS = setOf('壬', '癸')
    }

    // 오행 상생/상극 관계
    object Relations {
        const val GENERATING_FORWARD = 1   // 상생 정방향
        const val GENERATING_BACKWARD = 4  // 상생 역방향
        const val CONFLICTING_FORWARD = 2  // 상극 정방향
        const val CONFLICTING_BACKWARD = 3 // 상극 역방향
        const val ELEMENT_COUNT = 5
    }
}
