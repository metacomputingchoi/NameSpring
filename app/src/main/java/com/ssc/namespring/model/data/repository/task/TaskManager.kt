// model/data/repository/task/TaskManager.kt
package com.ssc.namespring.model.data.repository.task

import android.util.Log
import com.ssc.namespring.model.data.repository.task.interfaces.*
import com.ssc.namespring.model.domain.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskManager(
    private val dataService: ITaskDataService,
    private val updateService: ITaskUpdateService,
    private val repositoryScope: CoroutineScope
) {
    companion object {
        private const val TAG = "TaskManager"
    }

    fun saveTask(task: Task) {
        repositoryScope.launch {
            try {
                val saved = dataService.saveTask(task)
                if (!saved) {
                    Log.e(TAG, "Failed to save task: ${task.id}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving task", e)
            }
        }
    }

    fun updateTask(task: Task) {
        saveTask(task)
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus, errorMessage: String? = null) {
        repositoryScope.launch {
            try {
                val updated = updateService.updateTaskStatus(taskId, status, errorMessage)
                if (!updated) {
                    Log.e(TAG, "Failed to update task status: $taskId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating task status", e)
            }
        }
    }

    fun updateTaskProgress(taskId: String, progress: Int) {
        repositoryScope.launch {
            try {
                val updated = updateService.updateTaskProgress(taskId, progress)
                if (!updated) {
                    Log.e(TAG, "Failed to update task progress: $taskId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating task progress", e)
            }
        }
    }

    suspend fun getTask(taskId: String): Task? = withContext(Dispatchers.IO) {
        try {
            dataService.getTask(taskId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting task", e)
            null
        }
    }

    suspend fun deleteTask(taskId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            dataService.deleteTask(taskId)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task", e)
            false
        }
    }

    fun clearTaskHistory(profileId: String) {
        repositoryScope.launch {
            try {
                val cleared = dataService.clearTaskHistory(profileId)
                if (!cleared) {
                    Log.e(TAG, "Failed to clear task history: $profileId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing task history", e)
            }
        }
    }
}