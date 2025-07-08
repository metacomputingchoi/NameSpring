// model/domain/builder/DateTimeConverter.kt
package com.ssc.namespring.model.domain.builder

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

/**
 * 날짜/시간 변환을 담당
 */
internal class DateTimeConverter {

    fun convert(calendar: Calendar): LocalDateTime {
        return calendar.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }
}