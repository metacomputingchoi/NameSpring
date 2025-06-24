// model/application/service/SajuService.kt
package com.ssc.namespring.model.application.service

import com.ssc.namespring.model.domain.saju.entity.Saju
import com.ssc.namespring.model.domain.element.entity.ElementBalance
import com.ssc.namespring.model.infrastructure.repository.SajuRepository
import com.ssc.namespring.model.common.constants.Constants
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SajuService(private val sajuRepository: SajuRepository) {

    fun calculateSaju(year: Int, month: Int, day: Int, hour: Int, minute: Int): Saju {
        var colIdxAdd = 0
        val inputDateTime = LocalDateTime.of(year, month, day, hour, minute)
        if (inputDateTime.hour == Constants.LATE_NIGHT_HOUR && inputDateTime.minute >= Constants.LATE_NIGHT_MINUTE) {
            colIdxAdd = Constants.COL_INDEX_ADD
        }

        val record = sajuRepository.findByDate(year, month, day)
            ?: throw IllegalArgumentException("해당 날짜의 데이터를 찾을 수 없습니다.")

        val yeonju = record.yeonju
        val wolju = record.wolju
        val ilju = record.ilju

        val cheongan = ilju[0]
        val colIdx = when (cheongan) {
            '甲', '乙' -> 0
            '丙', '丁' -> 1
            '戊', '己' -> 2
            '庚', '辛' -> 3
            else -> 4
        }

        val hourMinute = inputDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val rowIdx = when {
            hourMinute >= Constants.TIME_RANGES[0].first || hourMinute < Constants.TIME_RANGES[0].second -> 0
            hourMinute < Constants.TIME_RANGES[1].second -> 1
            hourMinute < Constants.TIME_RANGES[2].second -> 2
            hourMinute < Constants.TIME_RANGES[3].second -> 3
            hourMinute < Constants.TIME_RANGES[4].second -> 4
            hourMinute < Constants.TIME_RANGES[5].second -> 5
            hourMinute < Constants.TIME_RANGES[6].second -> 6
            hourMinute < Constants.TIME_RANGES[7].second -> 7
            hourMinute < Constants.TIME_RANGES[8].second -> 8
            hourMinute < Constants.TIME_RANGES[9].second -> 9
            hourMinute < Constants.TIME_RANGES[10].second -> 10
            else -> 11
        }

        val finalColIdx = (colIdx + colIdxAdd) % Constants.COL_INDEX_MODULO
        val sijuValue = Constants.SIJU[rowIdx][finalColIdx]

        return Saju(yeonju, wolju, ilju, sijuValue)
    }

    fun calculateElementBalance(saju: Saju): ElementBalance {
        val elements = listOf(
            Constants.STEM_ELEMENTS[saju.yeonju[0].toString()],
            Constants.BRANCH_ELEMENTS[saju.yeonju[1].toString()],
            Constants.STEM_ELEMENTS[saju.wolju[0].toString()],
            Constants.BRANCH_ELEMENTS[saju.wolju[1].toString()],
            Constants.STEM_ELEMENTS[saju.ilju[0].toString()],
            Constants.BRANCH_ELEMENTS[saju.ilju[1].toString()],
            Constants.STEM_ELEMENTS[saju.siju[0].toString()],
            Constants.BRANCH_ELEMENTS[saju.siju[1].toString()]
        )

        return ElementBalance(
            wood = elements.count { it == "木" },
            fire = elements.count { it == "火" },
            earth = elements.count { it == "土" },
            metal = elements.count { it == "金" },
            water = elements.count { it == "水" }
        )
    }
}