// ui/history/handlers/ITaskActionHandler.kt
package com.ssc.namespring.ui.history.handlers

import com.ssc.namespring.model.domain.entity.Task

interface ITaskActionHandler {
    fun cancelTask(task: Task)
    fun retryTask(task: Task)
    fun deleteSelectedTasks(tasks: List<Task>, onComplete: () -> Unit)
}