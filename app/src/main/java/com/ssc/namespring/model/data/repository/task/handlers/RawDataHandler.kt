// model/data/repository/task/handlers/RawDataHandler.kt
package com.ssc.namespring.model.data.repository.task.handlers

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class RawDataHandler(private val context: Context) {
    companion object {
        private const val TAG = "RawDataHandler"
        private const val TASK_RESULTS_DIR = "task_results"
    }

    suspend fun save(taskId: String, data: String): String? = 
        withContext(Dispatchers.IO) {
            try {
                val fileName = "task_${taskId}_${System.currentTimeMillis()}.json"
                val dir = File(context.filesDir, TASK_RESULTS_DIR).apply {
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

    suspend fun load(filePath: String): String? = 
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (file.exists()) file.readText() else null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load raw data from $filePath", e)
                null
            }
        }
}