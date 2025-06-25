// model/util/TimeCalculator.kt
package com.ssc.namespring.model.util

import android.annotation.SuppressLint
import com.ssc.namespring.model.Constants.TimeSlot

object TimeCalculator {
    fun getColumnIndex(cheongan: Char): Int {
        return when (cheongan) {
            '甲', '乙' -> 0
            '丙', '丁' -> 1
            '戊', '己' -> 2
            '庚', '辛' -> 3
            else -> 4
        }
    }

    @SuppressLint("DefaultLocale")
    fun getRowIndex(hour: Int, minute: Int, second: Int): Int {
        val timeStr = String.format("%02d:%02d:%02d", hour, minute, second)

        return when {
            timeStr >= TimeSlot.SLOT_23_30 || timeStr < TimeSlot.SLOT_01_30 -> 0
            timeStr < TimeSlot.SLOT_03_30 -> 1
            timeStr < TimeSlot.SLOT_05_30 -> 2
            timeStr < TimeSlot.SLOT_07_30 -> 3
            timeStr < TimeSlot.SLOT_09_30 -> 4
            timeStr < TimeSlot.SLOT_11_30 -> 5
            timeStr < TimeSlot.SLOT_13_30 -> 6
            timeStr < TimeSlot.SLOT_15_30 -> 7
            timeStr < TimeSlot.SLOT_17_30 -> 8
            timeStr < TimeSlot.SLOT_19_30 -> 9
            timeStr < TimeSlot.SLOT_21_30 -> 10
            else -> 11
        }
    }
}
