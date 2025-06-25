// model/util/DateCalculator.kt
package com.ssc.namespring.model.util

import com.ssc.namespring.model.common.Constants
import com.ssc.namespring.model.common.Constants.DateConstants
import com.ssc.namespring.model.common.Constants.YAJASI_HOUR
import com.ssc.namespring.model.common.Constants.YAJASI_MINUTE

object DateCalculator {
    operator fun invoke(useYajasi: Boolean) = DateCalculator(useYajasi)

    class DateCalculator(private val useYajasi: Boolean) {
        fun adjustDate(year: Int, month: Int, day: Int, hour: Int, minute: Int): Tuple4<Int, Int, Int, Int> {
            var adjustedYear = year
            var adjustedMonth = month
            var adjustedDay = day
            var yeolidxAdd = 0

            if (hour == YAJASI_HOUR && minute >= YAJASI_MINUTE) {
                yeolidxAdd = Constants.YAJASI_DAY_INCREMENT
                if (!useYajasi) {
                    adjustedDay++
                    val adjustment = adjustForMonthEnd(adjustedYear, adjustedMonth, adjustedDay)
                    adjustedYear = adjustment.first
                    adjustedMonth = adjustment.second
                    adjustedDay = adjustment.third
                }
            }

            return Tuple4(adjustedYear, adjustedMonth, adjustedDay, yeolidxAdd)
        }

        private fun adjustForMonthEnd(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
            val maxDay = getMaxDayOfMonth(year, month)

            return if (day > maxDay) {
                if (month == DateConstants.DECEMBER) {
                    Triple(year + 1, 1, 1)
                } else {
                    Triple(year, month + 1, 1)
                }
            } else {
                Triple(year, month, day)
            }
        }

        private fun getMaxDayOfMonth(year: Int, month: Int): Int {
            return when (month) {
                DateConstants.FEBRUARY -> if (isLeapYear(year)) {
                    DateConstants.DAYS_IN_FEBRUARY_LEAP
                } else {
                    DateConstants.DAYS_IN_FEBRUARY
                }
                DateConstants.APRIL, DateConstants.JUNE, 
                DateConstants.SEPTEMBER, DateConstants.NOVEMBER -> DateConstants.DAYS_IN_SHORT_MONTH
                else -> DateConstants.DAYS_IN_LONG_MONTH
            }
        }

        private fun isLeapYear(year: Int): Boolean {
            return (year % DateConstants.LEAP_YEAR_DIVISOR == 0 && year % DateConstants.CENTURY_DIVISOR != 0) || 
                   (year % DateConstants.LEAP_CENTURY_DIVISOR == 0)
        }
    }

    data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
