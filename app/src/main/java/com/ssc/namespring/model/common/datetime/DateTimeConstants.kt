// model/common/datetime/DateTimeConstants.kt
package com.ssc.namespring.model.common.datetime

import java.time.LocalTime

object DateTimeConstants {

    // 시간대별 경계값
    object TimeSlots {
        val SLOT_BOUNDARIES = listOf(
            LocalTime.of(23, 30), // 자시
            LocalTime.of(1, 30),  // 축시
            LocalTime.of(3, 30),  // 인시
            LocalTime.of(5, 30),  // 묘시
            LocalTime.of(7, 30),  // 진시
            LocalTime.of(9, 30),  // 사시
            LocalTime.of(11, 30), // 오시
            LocalTime.of(13, 30), // 미시
            LocalTime.of(15, 30), // 신시
            LocalTime.of(17, 30), // 유시
            LocalTime.of(19, 30), // 술시
            LocalTime.of(21, 30)  // 해시
        )

        val SLOT_NAMES = listOf(
            "자시(子時)", "축시(丑時)", "인시(寅時)", "묘시(卯時)",
            "진시(辰時)", "사시(巳時)", "오시(午時)", "미시(未時)",
            "신시(申時)", "유시(酉時)", "술시(戌時)", "해시(亥時)"
        )
    }

    // 야자시 관련
    object Yajasi {
        val START_TIME = LocalTime.of(23, 30)
        const val DAY_INCREMENT = 1
    }
}
