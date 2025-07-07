// model/data/repository/task/TaskQueryService.kt
package com.ssc.namespring.model.data.repository.task

import androidx.lifecycle.LiveData
import com.ssc.namespring.model.domain.entity.*

class TaskQueryService(
    private val taskHistoryMap: LiveData<Map<String, TaskHistory>>
) {

    fun getTaskHistory(profileId: String): List<Task> {
        return taskHistoryMap.value?.get(profileId)?.tasks ?: emptyList()
    }

    fun getTaskCount(profileId: String, type: TaskType? = null, status: TaskStatus? = null): Int {
        return try {
            val tasks = getTaskHistory(profileId)
            tasks.count { task ->
                (type == null || task.type == type) &&
                (status == null || task.status == status)
            }
        } catch (e: Exception) {
            0
        }
    }

    fun getAllTasks(): List<Task> {
        return try {
            taskHistoryMap.value?.values?.flatMap { it.tasks } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}