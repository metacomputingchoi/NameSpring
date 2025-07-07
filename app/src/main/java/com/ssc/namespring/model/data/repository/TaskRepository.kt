package com.ssc.namespring.model.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.ssc.namespring.model.data.repository.task.*
import com.ssc.namespring.model.data.repository.task.interfaces.*
import com.ssc.namespring.model.domain.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class TaskRepository private constructor(private val context: Context) {

    companion object {
        private const val TAG = "TaskRepository"

        @Volatile
        private var INSTANCE: TaskRepository? = null

        fun getInstance(context: Context): TaskRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TaskRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val gson = Gson()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Services
    private val fileService: ITaskFileService = TaskFileService(context, gson)
    private val dataService: ITaskDataService = TaskDataService(fileService, repositoryScope)
    private val updateService: ITaskUpdateService = TaskUpdateService(dataService)
    private val resultService: ITaskResultService = TaskResultService(fileService)

    // Public properties
    val taskHistoryMap: LiveData<Map<String, TaskHistory>> = dataService.taskHistoryMap

    // Task management
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

    fun getTaskHistory(profileId: String): List<Task> {
        // This is synchronous for backward compatibility
        return taskHistoryMap.value?.get(profileId)?.tasks ?: emptyList()
    }

    // Task result management
    fun saveTaskResult(result: TaskResult) {
        repositoryScope.launch {
            try {
                val saved = resultService.saveTaskResult(result)
                if (!saved) {
                    Log.e(TAG, "Failed to save task result: ${result.taskId}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving task result", e)
            }
        }
    }

    fun getTaskResult(taskId: String): TaskResult? {
        // Synchronous for backward compatibility
        return try {
            val tasksDir = File(context.filesDir, "tasks")
            val file = File(tasksDir, "results_${taskId}.json")
            if (file.exists()) {
                gson.fromJson(file.readText(), TaskResult::class.java)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load task result", e)
            null
        }
    }

    suspend fun saveTaskResultWithFile(taskResult: TaskResult): TaskResult = 
        withContext(Dispatchers.IO) {
            try {
                resultService.saveTaskResultWithFile(taskResult) ?: taskResult
            } catch (e: Exception) {
                Log.e(TAG, "Error saving task result with file", e)
                taskResult
            }
        }

    suspend fun loadRawDataFromFile(filePath: String): String? = withContext(Dispatchers.IO) {
        try {
            resultService.loadRawDataFromFile(filePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading raw data from file", e)
            null
        }
    }

    // Task deletion
    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        try {
            // Get task result to find raw data file
            val result = getTaskResult(taskId)

            // Delete task
            val deleted = dataService.deleteTask(taskId)

            if (deleted) {
                // Delete raw data file if exists
                result?.data?.get("rawDataFile")?.let { filePath ->
                    try {
                        File(filePath.toString()).delete()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to delete raw data file", e)
                    }
                }
                Log.d(TAG, "Successfully deleted task: $taskId")
            } else {
                Log.e(TAG, "Failed to delete task: $taskId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task", e)
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

    // Maintenance
    suspend fun cleanupOldTaskFiles(daysToKeep: Int = 7) = withContext(Dispatchers.IO) {
        try {
            val cleaned = fileService.cleanupOldFiles(daysToKeep)
            if (!cleaned) {
                Log.e(TAG, "Failed to cleanup old task files")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old task files", e)
        }
    }

    // Utility methods
    fun getTaskCount(profileId: String, type: TaskType? = null, status: TaskStatus? = null): Int {
        return try {
            val tasks = getTaskHistory(profileId)
            tasks.count { task ->
                (type == null || task.type == type) &&
                (status == null || task.status == status)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting task count", e)
            0
        }
    }

    fun getAllTasks(): List<Task> {
        return try {
            taskHistoryMap.value?.values?.flatMap { it.tasks } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all tasks", e)
            emptyList()
        }
    }
}