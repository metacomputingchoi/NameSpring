// model/data/repository/task/TaskResultManager.kt
package com.ssc.namespring.model.data.repository.task

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.ssc.namespring.model.data.repository.task.interfaces.*
import com.ssc.namespring.model.domain.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class TaskResultManager(
    private val context: Context,
    private val resultService: ITaskResultService,
    private val gson: Gson,
    private val repositoryScope: CoroutineScope
) {
    companion object {
        private const val TAG = "TaskResultManager"
    }

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

    fun deleteResultFile(result: TaskResult?) {
        result?.data?.get("rawDataFile")?.let { filePath ->
            try {
                File(filePath.toString()).delete()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete raw data file", e)
            }
        }
    }
}