// model/util/DateCalculator.kt
package com.ssc.namespring.model.util

import com.ssc.namespring.model.common.datetime.DateTimeConstants

object DateCalculator {
    operator fun invoke(useYajasi: Boolean) = DateCalculator(useYajasi)

    class DateCalculator(private val useYajasi: Boolean) {
        fun adjustDate(year: Int, month: Int, day: Int, hour: Int, minute: Int): Tuple4<Int, Int, Int, Int> {
            var adjustedYear = year
            var adjustedMonth = month
            var adjustedDay = day
            var yeolidxAdd = 0

            if (hour == DateTimeConstants.YAJASI_HOUR && minute >= DateTimeConstants.YAJASI_MINUTE) {
                yeolidxAdd = DateTimeConstants.YAJASI_DAY_INCREMENT
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
                if (month == DateTimeConstants.Date.DECEMBER) {
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
                DateTimeConstants.Date.FEBRUARY -> if (isLeapYear(year)) {
                    DateTimeConstants.Date.DAYS_IN_FEBRUARY_LEAP
                } else {
                    DateTimeConstants.Date.DAYS_IN_FEBRUARY
                }
                DateTimeConstants.Date.APRIL, DateTimeConstants.Date.JUNE, 
                DateTimeConstants.Date.SEPTEMBER, DateTimeConstants.Date.NOVEMBER -> DateTimeConstants.Date.DAYS_IN_SHORT_MONTH
                else -> DateTimeConstants.Date.DAYS_IN_LONG_MONTH
            }
        }

        private fun isLeapYear(year: Int): Boolean {
            return (year % DateTimeConstants.Date.LEAP_YEAR_DIVISOR == 0 && year % DateTimeConstants.Date.CENTURY_DIVISOR != 0) || 
                   (year % DateTimeConstants.Date.LEAP_CENTURY_DIVISOR == 0)
        }
    }

    data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
