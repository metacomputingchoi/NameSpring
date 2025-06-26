// model/service/SajuCalculator.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.datetime.DateTimeConstants
import com.ssc.namespring.model.common.saju.SajuConstants
import com.ssc.namespring.model.common.parsing.ParsingConstants
import com.ssc.namespring.model.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.repository.DataRepository
import java.time.LocalDateTime
import java.time.LocalTime

class SajuCalculator(private val dataRepository: DataRepository) {

    fun getSaju(
        birthDateTime: LocalDateTime,
        useYajasi: Boolean = true
    ): Array<String> {
        try {
            val dateTime = birthDateTime

            // 야자시 처리
            var adjustedDateTime = dateTime
            var yeolidxAdd = 0

            if (dateTime.toLocalTime() >= DateTimeConstants.Yajasi.START_TIME) {
                yeolidxAdd = DateTimeConstants.Yajasi.DAY_INCREMENT
                if (!useYajasi) {
                    adjustedDateTime = adjustedDateTime.plusDays(1)
                }
            }

            val result = dataRepository.ymdData.find { data ->
                data[ParsingConstants.JsonKeys.YEAR] == adjustedDateTime.year &&
                data[ParsingConstants.JsonKeys.MONTH] == adjustedDateTime.monthValue &&
                data[ParsingConstants.JsonKeys.DAY] == adjustedDateTime.dayOfMonth
            } ?: throw NamingException.DataNotFoundException(
                ParsingConstants.ErrorMessages.DATE_NOT_FOUND,
                dataType = "사주 데이터",
                searchKey = "${adjustedDateTime.year}-${adjustedDateTime.monthValue}-${adjustedDateTime.dayOfMonth}"
            )

            val yeonju = result[ParsingConstants.JsonKeys.YEAR_PILLAR] as String
            val wolju = result[ParsingConstants.JsonKeys.MONTH_PILLAR] as String
            val ilju = result[ParsingConstants.JsonKeys.DAY_PILLAR] as String

            val colIdx = getColumnIndex(ilju[0])
            val rowIdx = getTimeSlotIndex(dateTime.toLocalTime())
            val adjustedColIdx = (colIdx + yeolidxAdd) % SajuConstants.Relations.ELEMENT_COUNT
            val siju = SajuConstants.SIJU[rowIdx][adjustedColIdx]

            return arrayOf(yeonju, wolju, ilju, siju)

        } catch (e: NamingException) {
            throw e
        }
    }

    private fun getColumnIndex(cheongan: Char): Int {
        return when (cheongan) {
            in SajuConstants.StemGroups.WOOD_STEMS -> 0
            in SajuConstants.StemGroups.FIRE_STEMS -> 1
            in SajuConstants.StemGroups.EARTH_STEMS -> 2
            in SajuConstants.StemGroups.METAL_STEMS -> 3
            in SajuConstants.StemGroups.WATER_STEMS -> 4
            else -> -1
        }
    }

    private fun getTimeSlotIndex(time: LocalTime): Int {
        val boundaries = DateTimeConstants.TimeSlots.SLOT_BOUNDARIES

        return when {
            time >= boundaries[0] || time < boundaries[1] -> 0
            time < boundaries[2] -> 1
            time < boundaries[3] -> 2
            time < boundaries[4] -> 3
            time < boundaries[5] -> 4
            time < boundaries[6] -> 5
            time < boundaries[7] -> 6
            time < boundaries[8] -> 7
            time < boundaries[9] -> 8
            time < boundaries[10] -> 9
            time < boundaries[11] -> 10
            else -> 11
        }
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
