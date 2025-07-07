// model/data/repository/task/interfaces/ITaskDataService.kt
package com.ssc.namespring.model.data.repository.task.interfaces

import androidx.lifecycle.LiveData
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskHistory
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namespring.model.domain.entity.TaskType

interface ITaskDataService {
    val taskHistoryMap: LiveData<Map<String, TaskHistory>>

    suspend fun saveTask(task: Task): Boolean
    suspend fun updateTask(task: Task): Boolean
    suspend fun deleteTask(taskId: String): Boolean
    suspend fun getTask(taskId: String): Task?
    suspend fun getTaskHistory(profileId: String): List<Task>
    suspend fun getAllTasks(): List<Task>
    suspend fun clearTaskHistory(profileId: String): Boolean
    suspend fun getTaskCount(profileId: String, type: TaskType? = null, status: TaskStatus? = null): Int
}