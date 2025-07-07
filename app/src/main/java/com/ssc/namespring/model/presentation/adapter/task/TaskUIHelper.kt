// model/presentation/adapter/task/TaskUIHelper.kt
package com.ssc.namespring.model.presentation.adapter.task

import android.graphics.Color
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namespring.model.domain.entity.TaskType
import java.text.SimpleDateFormat
import java.util.*

object TaskUIHelper {
    private val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    fun getTaskName(task: Task): String {
        return task.inputData["profileName"] as? String ?: "작업 ${task.id.take(8)}"
    }

    fun getTaskTypeText(type: TaskType): String = when (type) {
        TaskType.NAMING -> "작명"
        TaskType.EVALUATION -> "평가"
        TaskType.COMPARISON -> "비교"
        TaskType.REPORT_GENERATION -> "보고서"
    }

    fun getTaskStatusText(task: Task, queuePosition: Int): String = when (task.status) {
        TaskStatus.PENDING -> "대기중 #$queuePosition"
        TaskStatus.RUNNING -> "진행중"
        TaskStatus.COMPLETED -> "완료"
        TaskStatus.FAILED -> "실패"
        TaskStatus.CANCELLED -> "취소됨"
    }

    fun getStatusColor(status: TaskStatus): Int = when (status) {
        TaskStatus.RUNNING -> Color.parseColor("#2196F3")
        TaskStatus.COMPLETED -> Color.parseColor("#4CAF50")
        TaskStatus.FAILED -> Color.parseColor("#F44336")
        TaskStatus.CANCELLED -> Color.parseColor("#FF9800")
        else -> Color.parseColor("#FFC107")
    }

    fun getActionButtonResource(status: TaskStatus): Pair<Int, String>? = when (status) {
        TaskStatus.RUNNING, TaskStatus.PENDING -> R.drawable.ic_cancel to "작업 취소"
        TaskStatus.CANCELLED, TaskStatus.FAILED -> R.drawable.ic_replay to "다시 시도"
        else -> null
    }

    fun formatTaskTime(timestamp: Long): String = dateFormat.format(Date(timestamp))

    fun formatDuration(task: Task): String {
        if (task.completedAt == null || task.startedAt == null) return ""

        val duration = (task.completedAt - task.startedAt) / 1000
        return when {
            duration < 60 -> "${duration}초"
            duration < 3600 -> "${duration / 60}분"
            else -> "${duration / 3600}시간"
        }
    }

    fun getSelectionBackgroundColor(isSelected: Boolean): Int =
        if (isSelected) Color.parseColor("#E3F2FD") else Color.WHITE
}