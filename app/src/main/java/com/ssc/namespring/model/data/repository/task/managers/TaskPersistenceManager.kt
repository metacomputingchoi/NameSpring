// model/data/repository/task/managers/TaskPersistenceManager.kt
package com.ssc.namespring.model.data.repository.task.managers

import android.util.Log
import com.ssc.namespring.model.data.repository.task.interfaces.ITaskFileService
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class TaskPersistenceManager(
    private val fileService: ITaskFileService
) {
    companion object {
        private const val TAG = "TaskPersistenceManager"
    }

    suspend fun loadAllHistories(): Map<String, TaskHistory> = withContext(Dispatchers.IO) {
        try {
            fileService.loadAllTaskHistories()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load all histories", e)
            emptyMap()
        }
    }

    suspend fun saveTaskHistory(profileId: String, tasks: List<Task>): Boolean = 
        withContext(Dispatchers.IO) {
            try {
                fileService.saveTaskHistory(profileId, tasks)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save task history", e)
                false
            }
        }

    suspend fun deleteTaskFiles(taskId: String) {
        try {
            fileService.deleteTaskFiles(taskId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete task files", e)
        }
    }

    suspend fun deleteTaskHistoryFile(profileId: String) {
        try {
            fileService.deleteTaskHistoryFile(profileId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete task history file", e)
        }
    }
}