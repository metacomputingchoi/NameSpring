// model/data/repository/task/handlers/TaskResultHandler.kt
package com.ssc.namespring.model.data.repository.task.handlers

import android.util.Log
import com.google.gson.Gson
import com.ssc.namespring.model.domain.entity.TaskResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class TaskResultHandler(
    private val tasksDir: File,
    private val gson: Gson,
    private val fileMutex: Mutex
) {
    companion object {
        private const val TAG = "TaskResultHandler"
        private const val TASK_RESULT_PREFIX = "results_"
    }

    suspend fun save(result: TaskResult): Boolean = 
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

    suspend fun load(taskId: String): TaskResult? = 
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

    suspend fun delete(taskId: String): Boolean = 
        withContext(Dispatchers.IO) {
            try {
                val file = File(tasksDir, "${TASK_RESULT_PREFIX}${taskId}.json")
                if (file.exists()) {
                    file.delete()
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete task result for $taskId", e)
                false
            }
        }
}