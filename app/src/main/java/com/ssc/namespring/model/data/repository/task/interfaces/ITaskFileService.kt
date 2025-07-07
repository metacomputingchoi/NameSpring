// model/data/repository/task/interfaces/ITaskFileService.kt
package com.ssc.namespring.model.data.repository.task.interfaces

import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskHistory
import com.ssc.namespring.model.domain.entity.TaskResult

interface ITaskFileService {
    suspend fun saveTaskHistory(profileId: String, tasks: List<Task>): Boolean
    suspend fun loadTaskHistory(profileId: String): TaskHistory?
    suspend fun loadAllTaskHistories(): Map<String, TaskHistory>
    suspend fun saveTaskResult(result: TaskResult): Boolean
    suspend fun loadTaskResult(taskId: String): TaskResult?
    suspend fun saveRawDataToFile(taskId: String, data: String): String?
    suspend fun loadRawDataFromFile(filePath: String): String?
    suspend fun deleteTaskFiles(taskId: String): Boolean
    suspend fun deleteTaskHistoryFile(profileId: String): Boolean
    suspend fun cleanupOldFiles(daysToKeep: Int): Boolean
}