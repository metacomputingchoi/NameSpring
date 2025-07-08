// model/data/repository/task/managers/TaskCRUDManager.kt
package com.ssc.namespring.model.data.repository.task.managers

import android.util.Log
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskHistory
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namespring.model.domain.entity.TaskType

internal class TaskCRUDManager {
    companion object {
        private const val TAG = "TaskCRUDManager"
    }

    fun addOrUpdateTask(history: TaskHistory, task: Task): List<Task> {
        val updatedTasks = history.tasks.toMutableList()
        val existingIndex = updatedTasks.indexOfFirst { it.id == task.id }

        if (existingIndex >= 0) {
            updatedTasks[existingIndex] = task
        } else {
            updatedTasks.add(task)
        }

        return updatedTasks
    }

    fun removeTask(tasks: List<Task>, taskId: String): List<Task> {
        return tasks.filter { it.id != taskId }
    }

    fun findTaskInHistories(
        histories: Map<String, TaskHistory>, 
        taskId: String
    ): Pair<String, Task>? {
        for ((profileId, history) in histories) {
            val task = history.tasks.find { it.id == taskId }
            if (task != null) {
                return profileId to task
            }
        }
        return null
    }

    fun getTask(histories: Map<String, TaskHistory>, taskId: String): Task? {
        return histories.values.flatMap { it.tasks }.find { it.id == taskId }
    }

    fun countTasks(
        tasks: List<Task>, 
        type: TaskType?, 
        status: TaskStatus?
    ): Int {
        return tasks.count { task ->
            (type == null || task.type == type) &&
            (status == null || task.status == status)
        }
    }
}