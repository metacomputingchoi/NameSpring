// model/service/FourPillarsCalculator.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.Constants
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.repository.DataRepository
import com.ssc.namespring.model.util.DateCalculator
import com.ssc.namespring.model.util.TimeCalculator

class FourPillarsCalculator(private val dataRepository: DataRepository) {

    fun get4ju(
        year: Int, month: Int, day: Int,
        hour: Int, minute: Int, second: Int = 0,
        useYajasi: Boolean = true
    ): Array<String> {
        val dateCalculator = DateCalculator(useYajasi)
        val (adjustedYear, adjustedMonth, adjustedDay, colIdxAdd) =
            dateCalculator.adjustDate(year, month, day, hour, minute)

        val result = dataRepository.ymdData.find { data ->
            data[Constants.JsonKeys.YEAR] == adjustedYear &&
                    data[Constants.JsonKeys.MONTH] == adjustedMonth &&
                    data[Constants.JsonKeys.DAY] == adjustedDay
        } ?: throw NamingException.DataNotFoundException(Constants.ErrorMessages.DATE_NOT_FOUND)

        val yeonju = result[Constants.JsonKeys.YEAR_PILLAR] as String
        val wolju = result[Constants.JsonKeys.MONTH_PILLAR] as String
        val ilju = result[Constants.JsonKeys.DAY_PILLAR] as String

        val colIdx = TimeCalculator.getColumnIndex(ilju[0])
        val rowIdx = TimeCalculator.getRowIndex(hour, minute, second)
        val adjustedColIdx = (colIdx + colIdxAdd) % 5
        val siju = Constants.SIJU[rowIdx][adjustedColIdx]

        return arrayOf(yeonju, wolju, ilju, siju)
    }

    fun getDictElementsCount(yeonju: String, wolju: String, ilju: String, siju: String): Map<String, Int> {
        val elements = listOf(
            Constants.STEM_ELEMENTS[yeonju[0].toString()],
            Constants.BRANCH_ELEMENTS[yeonju[1].toString()],
            Constants.STEM_ELEMENTS[wolju[0].toString()],
            Constants.BRANCH_ELEMENTS[wolju[1].toString()],
            Constants.STEM_ELEMENTS[ilju[0].toString()],
            Constants.BRANCH_ELEMENTS[ilju[1].toString()],
            Constants.STEM_ELEMENTS[siju[0].toString()],
            Constants.BRANCH_ELEMENTS[siju[1].toString()]
        )

        return Constants.ELEMENTS_ORDER.associateWith { element ->
            elements.count { it == element }
        }
    }
}