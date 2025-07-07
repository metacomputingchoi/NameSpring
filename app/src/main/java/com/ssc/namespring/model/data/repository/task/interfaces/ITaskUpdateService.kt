// model/data/repository/task/interfaces/ITaskUpdateService.kt
package com.ssc.namespring.model.data.repository.task.interfaces

import com.ssc.namespring.model.domain.entity.TaskStatus

interface ITaskUpdateService {
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus, errorMessage: String? = null): Boolean
    suspend fun updateTaskProgress(taskId: String, progress: Int): Boolean
}