// model/data/repository/task/TaskResultService.kt
package com.ssc.namespring.model.data.repository.task

import android.util.Log
import com.ssc.namespring.model.data.repository.task.interfaces.ITaskFileService
import com.ssc.namespring.model.data.repository.task.interfaces.ITaskResultService
import com.ssc.namespring.model.domain.entity.TaskResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class TaskResultService(
    private val fileService: ITaskFileService
) : ITaskResultService {

    companion object {
        private const val TAG = "TaskResultService"
        private const val LARGE_DATA_THRESHOLD = 5000
    }

    override suspend fun saveTaskResult(result: TaskResult): Boolean = 
        withContext(Dispatchers.IO) {
            try {
                fileService.saveTaskResult(result)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save task result", e)
                false
            }
        }

    override suspend fun saveTaskResultWithFile(taskResult: TaskResult): TaskResult? =
        withContext(Dispatchers.IO) {
            try {
                // Check if raw data should be saved as file
                val modifiedResult = if (shouldSaveAsFile(taskResult)) {
                    val filePath = fileService.saveRawDataToFile(
                        taskResult.taskId, 
                        taskResult.rawData!!
                    )

                    if (filePath != null) {
                        taskResult.copy(
                            rawData = null,
                            data = (taskResult.data ?: emptyMap()) + 
                                   mapOf("rawDataFile" to filePath)
                        )
                    } else {
                        // Failed to save to file, keep in memory
                        taskResult
                    }
                } else {
                    taskResult
                }

                // Save the result
                val saved = fileService.saveTaskResult(modifiedResult)
                if (saved) {
                    modifiedResult
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save task result with file", e)
                null
            }
        }

    override suspend fun getTaskResult(taskId: String): TaskResult? = 
        withContext(Dispatchers.IO) {
            try {
                fileService.loadTaskResult(taskId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get task result for $taskId", e)
                null
            }
        }

    override suspend fun loadRawDataFromFile(filePath: String): String? = 
        withContext(Dispatchers.IO) {
            try {
                fileService.loadRawDataFromFile(filePath)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load raw data from file", e)
                null
            }
        }

    private fun shouldSaveAsFile(taskResult: TaskResult): Boolean {
        return taskResult.rawData != null && 
               taskResult.rawData.length > LARGE_DATA_THRESHOLD
    }
}