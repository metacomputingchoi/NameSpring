// model/data/repository/task/handlers/TaskHistoryHandler.kt
package com.ssc.namespring.model.data.repository.task.handlers

import android.util.Log
import com.google.gson.Gson
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class TaskHistoryHandler(
    private val tasksDir: File,
    private val gson: Gson,
    private val fileMutex: Mutex
) {
    companion object {
        private const val TAG = "TaskHistoryHandler"
        private const val TASK_FILE_PREFIX = "tasks_"
    }

    suspend fun save(profileId: String, tasks: List<Task>): Boolean = 
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

    suspend fun load(profileId: String): TaskHistory? = 
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

    suspend fun loadAll(): Map<String, TaskHistory> = 
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

    suspend fun delete(profileId: String): Boolean = 
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
}