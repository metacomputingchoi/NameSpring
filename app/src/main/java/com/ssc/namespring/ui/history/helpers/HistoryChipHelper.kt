// ui/history/helpers/HistoryChipHelper.kt
package com.ssc.namespring.ui.history.helpers

import android.content.Context
import com.google.android.material.chip.Chip
import com.ssc.namespring.model.domain.entity.TaskType

object HistoryChipHelper {
    fun createTypeChip(context: Context, type: TaskType, onChecked: () -> Unit): Chip {
        return Chip(context).apply {
            text = getTaskTypeName(type)
            isCheckable = true
            setOnCheckedChangeListener { _, _ -> onChecked() }
        }
    }

    private fun getTaskTypeName(type: TaskType): String {
        return when (type) {
            TaskType.NAMING -> "작명"
            TaskType.EVALUATION -> "평가"
            TaskType.COMPARISON -> "비교"
            TaskType.REPORT_GENERATION -> "보고서"
        }
    }
}