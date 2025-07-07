// model/data/repository/task/TaskFileService.kt
package com.ssc.namespring.model.data.repository.task

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.ssc.namespring.model.data.repository.task.interfaces.ITaskFileService
import com.ssc.namespring.model.domain.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class TaskFileService(
    private val context: Context,
    private val gson: Gson
) : ITaskFileService {

    companion object {
        private const val TAG = "TaskFileService"
        private const val TASK_FILE_PREFIX = "tasks_"
        private const val TASK_RESULT_PREFIX = "results_"
    }

    private val tasksDir = File(context.filesDir, "tasks").apply { mkdirs() }
    private val fileMutex = Mutex()

    override suspend fun saveTaskHistory(profileId: String, tasks: List<Task>): Boolean = 
        withContext(Dispatchers.IO) {
            fileMutex.withLock {
                try {
                    val file = File(tasksDir, "${TASK_FILE_PREFIX}${profileId}.json")
                    val history = TaskHistory(profileId, tasks)
                    file.writeText(gson.toJson(history))
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save task history for $profileId", e)
                    false
                }
            }
        }

    override suspend fun loadTaskHistory(profileId: String): TaskHistory? = 
        withContext(Dispatchers.IO) {
            try {
                val file = File(tasksDir, "${TASK_FILE_PREFIX}${profileId}.json")
                if (file.exists()) {
                    gson.fromJson(file.readText(), TaskHistory::class.java)
                } else null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load task history for $profileId", e)
                null
            }
        }

    override suspend fun loadAllTaskHistories(): Map<String, TaskHistory> = 
        withContext(Dispatchers.IO) {
            val historyMap = mutableMapOf<String, TaskHistory>()
            try {
                tasksDir.listFiles { file -> 
                    file.name.startsWith(TASK_FILE_PREFIX) 
                }?.forEach { file ->
                    try {
                        val history = gson.fromJson(file.readText(), TaskHistory::class.java)
                        historyMap[history.profileId] = history
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load history from ${file.name}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load all task histories", e)
            }
            historyMap
        }

    override suspend fun saveTaskResult(result: TaskResult): Boolean = 
        withContext(Dispatchers.IO) {
            fileMutex.withLock {
                try {
                    val file = File(tasksDir, "${TASK_RESULT_PREFIX}${result.taskId}.json")
                    file.writeText(gson.toJson(result))
                    Log.d(TAG, "Saved task result for ${result.taskId}")
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save task result", e)
                    false
                }
            }
        }

    override suspend fun loadTaskResult(taskId: String): TaskResult? = 
        withContext(Dispatchers.IO) {
            try {
                val file = File(tasksDir, "${TASK_RESULT_PREFIX}${taskId}.json")
                if (file.exists()) {
                    gson.fromJson(file.readText(), TaskResult::class.java)
                } else null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load task result for $taskId", e)
                null
            }
        }

    override suspend fun saveRawDataToFile(taskId: String, data: String): String? = 
        withContext(Dispatchers.IO) {
            try {
                val fileName = "task_${taskId}_${System.currentTimeMillis()}.json"
                val dir = File(context.filesDir, "task_results").apply {
                    if (!exists()) mkdirs()
                }
                val targetFile = File(dir, fileName)
                targetFile.writeText(data)
                targetFile.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save raw data to file", e)
                null
            }
        }

    override suspend fun loadRawDataFromFile(filePath: String): String? = 
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (file.exists()) file.readText() else null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load raw data from $filePath", e)
                null
            }
        }

    override suspend fun deleteTaskFiles(taskId: String): Boolean = 
        withContext(Dispatchers.IO) {
            try {
                val resultFile = File(tasksDir, "${TASK_RESULT_PREFIX}${taskId}.json")
                if (resultFile.exists()) {
                    resultFile.delete()
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete task files for $taskId", e)
                false
            }
        }

    override suspend fun deleteTaskHistoryFile(profileId: String): Boolean = 
        withContext(Dispatchers.IO) {
            try {
                val file = File(tasksDir, "${TASK_FILE_PREFIX}${profileId}.json")
                if (file.exists()) {
                    file.delete()
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete task history file for $profileId", e)
                false
            }
        }

    override suspend fun cleanupOldFiles(daysToKeep: Int): Boolean = 
        withContext(Dispatchers.IO) {
            try {
                val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)

                // Clean up task result files
                val taskResultsDir = File(context.filesDir, "task_results")
                taskResultsDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < cutoffTime) {
                        file.delete()
                    }
                }

                // Clean up old result files
                tasksDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < cutoffTime && 
                        file.name.startsWith(TASK_RESULT_PREFIX)) {
                        file.delete()
                    }
                }

                Log.d(TAG, "Cleaned up old task files")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cleanup old files", e)
                false
            }
        }
}