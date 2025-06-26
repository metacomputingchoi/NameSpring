// model/service/SajuCalculator.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.saju.SajuConstants
import com.ssc.namespring.model.common.parsing.ParsingConstants
import com.ssc.namespring.model.data.analysis.component.SajuAnalysisInfo
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
            data[ParsingConstants.JsonKeys.YEAR] == adjustedYear &&
                    data[ParsingConstants.JsonKeys.MONTH] == adjustedMonth &&
                    data[ParsingConstants.JsonKeys.DAY] == adjustedDay
        } ?: throw NamingException.DataNotFoundException(ParsingConstants.ErrorMessages.DATE_NOT_FOUND)

        val yeonju = result[ParsingConstants.JsonKeys.YEAR_PILLAR] as String
        val wolju = result[ParsingConstants.JsonKeys.MONTH_PILLAR] as String
        val ilju = result[ParsingConstants.JsonKeys.DAY_PILLAR] as String

        val colIdx = TimeCalculator.getColumnIndex(ilju[0])
        val rowIdx = TimeCalculator.getRowIndex(hour, minute, second)
        val adjustedColIdx = (colIdx + yeolidxAdd) % SajuConstants.Relations.ELEMENT_COUNT
        val siju = SajuConstants.SIJU[rowIdx][adjustedColIdx]

        return arrayOf(yeonju, wolju, ilju, siju)
    }

    fun getSajuOhaengCount(yeonju: String, wolju: String, ilju: String, siju: String): Map<String, Int> {
        val elements = listOf(
            SajuConstants.CHEONGAN_OHAENG[yeonju[0].toString()],
            SajuConstants.JIJI_OHAENG[yeonju[1].toString()],
            SajuConstants.CHEONGAN_OHAENG[wolju[0].toString()],
            SajuConstants.JIJI_OHAENG[wolju[1].toString()],
            SajuConstants.CHEONGAN_OHAENG[ilju[0].toString()],
            SajuConstants.JIJI_OHAENG[ilju[1].toString()],
            SajuConstants.CHEONGAN_OHAENG[siju[0].toString()],
            SajuConstants.JIJI_OHAENG[siju[1].toString()]
        )

        return SajuConstants.OHAENG_SUNSE.associateWith { element ->
            elements.count { it == element }
        }
    }

    fun analyzeSaju(fourPillars: Array<String>, sajuOhaengCount: Map<String, Int>): SajuAnalysisInfo {
        val missingElements = sajuOhaengCount.filterValues { it == 0 }.keys.toList()
        val dominantElements = sajuOhaengCount.filterValues { it >= 3 }.keys.toList()
        val totalElements = sajuOhaengCount.values.sum()

        val elementBalance = sajuOhaengCount.mapValues { (_, count) ->
            count.toFloat() / totalElements
        }

        return SajuAnalysisInfo(
            fourPillars = fourPillars,
            sajuOhaengCount = sajuOhaengCount,
            missingElements = missingElements,
            dominantElements = dominantElements,
            elementBalance = elementBalance
        )
    }
}
