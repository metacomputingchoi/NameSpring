// model/service/SajuCalculator.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.Constants
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.repository.DataRepository
import com.ssc.namespring.model.util.DateCalculator
import com.ssc.namespring.model.util.TimeCalculator

class SajuCalculator(private val dataRepository: DataRepository) {

    fun getSaju(
        year: Int, month: Int, day: Int,
        hour: Int, minute: Int, second: Int = 0,
        useYajasi: Boolean = true
    ): Array<String> {
        val dateCalculator = DateCalculator(useYajasi)
        val (adjustedYear, adjustedMonth, adjustedDay, yeolidxAdd) =
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
        val adjustedColIdx = (colIdx + yeolidxAdd) % Constants.SangsaengSanggeukRelations.ELEMENT_COUNT
        val siju = Constants.SIJU[rowIdx][adjustedColIdx]

        return arrayOf(yeonju, wolju, ilju, siju)
    }

    fun getSajuOhaengCount(yeonju: String, wolju: String, ilju: String, siju: String): Map<String, Int> {
        val elements = listOf(
            Constants.CHEONGAN_OHAENG[yeonju[0].toString()],
            Constants.JIJI_OHAENG[yeonju[1].toString()],
            Constants.CHEONGAN_OHAENG[wolju[0].toString()],
            Constants.JIJI_OHAENG[wolju[1].toString()],
            Constants.CHEONGAN_OHAENG[ilju[0].toString()],
            Constants.JIJI_OHAENG[ilju[1].toString()],
            Constants.CHEONGAN_OHAENG[siju[0].toString()],
            Constants.JIJI_OHAENG[siju[1].toString()]
        )

        return Constants.OHAENG_SUNSE.associateWith { element ->
            elements.count { it == element }
        }
    }
}
