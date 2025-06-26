// model/common/datetime/DateTimeConstants.kt
package com.ssc.namespring.model.common.datetime

object DateTimeConstants {
    // 날짜 관련 상수
    object Date {
        const val FEBRUARY = 2
        const val APRIL = 4
        const val JUNE = 6
        const val SEPTEMBER = 9
        const val NOVEMBER = 11
        const val DECEMBER = 12

        const val DAYS_IN_FEBRUARY = 28
        const val DAYS_IN_FEBRUARY_LEAP = 29
        const val DAYS_IN_SHORT_MONTH = 30
        const val DAYS_IN_LONG_MONTH = 31

        const val LEAP_YEAR_DIVISOR = 4
        const val CENTURY_DIVISOR = 100
        const val LEAP_CENTURY_DIVISOR = 400
    }

    // 시간 구분 상수
    object TimeSlot {
        const val SLOT_23_30 = "23:30:00"
        const val SLOT_01_30 = "01:30:00"
        const val SLOT_03_30 = "03:30:00"
        const val SLOT_05_30 = "05:30:00"
        const val SLOT_07_30 = "07:30:00"
        const val SLOT_09_30 = "09:30:00"
        const val SLOT_11_30 = "11:30:00"
        const val SLOT_13_30 = "13:30:00"
        const val SLOT_15_30 = "15:30:00"
        const val SLOT_17_30 = "17:30:00"
        const val SLOT_19_30 = "19:30:00"
        const val SLOT_21_30 = "21:30:00"
    }

    // 야자시 관련
    const val YAJASI_HOUR = 23
    const val YAJASI_MINUTE = 30
    const val YAJASI_DAY_INCREMENT = 1
}
