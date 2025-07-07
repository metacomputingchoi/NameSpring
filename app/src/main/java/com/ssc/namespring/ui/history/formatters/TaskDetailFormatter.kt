// ui/history/formatters/TaskDetailFormatter.kt
package com.ssc.namespring.ui.history.formatters

import com.ssc.namespring.model.domain.entity.TaskType
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailFormatter {

    fun getTaskTypeName(type: TaskType): String {
        return when (type) {
            TaskType.NAMING -> "작명"
            TaskType.EVALUATION -> "평가"
            TaskType.COMPARISON -> "비교"
            TaskType.REPORT_GENERATION -> "보고서 생성"
        }
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDuration(duration: Long): String {
        val seconds = duration / 1000
        return when {
            seconds < 60 -> "${seconds}초"
            seconds < 3600 -> "${seconds / 60}분 ${seconds % 60}초"
            else -> "${seconds / 3600}시간 ${(seconds % 3600) / 60}분"
        }
    }
}