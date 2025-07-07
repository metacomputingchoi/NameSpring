// model/data/repository/task/interfaces/ITaskResultService.kt
package com.ssc.namespring.model.data.repository.task.interfaces

import com.ssc.namespring.model.domain.entity.TaskResult

interface ITaskResultService {
    suspend fun saveTaskResult(result: TaskResult): Boolean
    suspend fun saveTaskResultWithFile(taskResult: TaskResult): TaskResult?
    suspend fun getTaskResult(taskId: String): TaskResult?
    suspend fun loadRawDataFromFile(filePath: String): String?
}