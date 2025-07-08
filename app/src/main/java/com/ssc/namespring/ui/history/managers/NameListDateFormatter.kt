// ui/history/managers/NameListDateFormatter.kt
package com.ssc.namespring.ui.history.managers

import com.ssc.namespring.model.domain.entity.Task
import java.text.SimpleDateFormat
import java.util.*

class NameListDateFormatter {
    private val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREAN)

    data class DateInfo(
        val millis: Long = 0L,
        val formatted: String = ""
    )

    fun extractDateInfo(task: Task): DateInfo {
        task.inputData["birthDateTime"]?.let { birthDateTimeStr ->
            (birthDateTimeStr as? String)?.toLongOrNull()?.let { millis ->
                return DateInfo(
                    millis = millis,
                    formatted = dateFormat.format(Date(millis))
                )
            }
        }
        return DateInfo()
    }
}