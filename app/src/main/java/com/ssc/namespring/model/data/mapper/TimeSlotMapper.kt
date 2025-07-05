// model/data/mapper/TimeSlotMapper.kt
package com.ssc.namespring.model.data.mapper

object TimeSlotMapper {
    fun getTimeSlotName(hour: Int): String {
        return when (hour) {
            23, 0 -> "자시(子時)"
            1, 2 -> "축시(丑時)"
            3, 4 -> "인시(寅時)"
            5, 6 -> "묘시(卯時)"
            7, 8 -> "진시(辰時)"
            9, 10 -> "사시(巳時)"
            11, 12 -> "오시(午時)"
            13, 14 -> "미시(未時)"
            15, 16 -> "신시(申時)"
            17, 18 -> "유시(酉時)"
            19, 20 -> "술시(戌時)"
            21, 22 -> "해시(亥時)"
            else -> ""
        }
    }
}