// ui/main/MainTaskProgressObserver.kt
package com.ssc.namespring.ui.main

import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.model.domain.service.workmanager.TaskWorkManager

class MainTaskProgressObserver(
    private val activity: AppCompatActivity,
    private val uiComponents: MainUIComponents
) {
    private val taskWorkManager = TaskWorkManager.getInstance(activity)

    fun observeTaskProgress(profileId: String) {
        taskWorkManager.taskHistory.observe(activity) { taskHistoryMap ->
            val history = taskHistoryMap[profileId]
            val activeTasks = history?.getActiveTasks() ?: emptyList()

            if (activeTasks.isNotEmpty()) {
                updateTaskIndicator(activeTasks)
            } else {
                resetTaskIndicator()
            }
        }
    }

    private fun updateTaskIndicator(activeTasks: List<Task>) {
        activeTasks.forEach { task ->
            when (task.type) {
                TaskType.NAMING -> {
                    uiComponents.updateButtonText(
                        MainUIComponents.ButtonType.NAMING,
                        "작명 (진행중...)"
                    )
                }
                TaskType.EVALUATION -> {
                    uiComponents.updateButtonText(
                        MainUIComponents.ButtonType.EVALUATION,
                        "평가 (진행중...)"
                    )
                }
                else -> {}
            }
        }
    }

    private fun resetTaskIndicator() {
        uiComponents.resetAllButtonTexts()
    }
}