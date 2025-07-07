// model/data/repository/task/services/FileCleanupService.kt
package com.ssc.namespring.model.data.repository.task.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileCleanupService(
    private val context: Context,
    private val tasksDir: File
) {
    companion object {
        private const val TAG = "FileCleanupService"
        private const val TASK_RESULT_PREFIX = "results_"
    }

    suspend fun cleanupOldFiles(daysToKeep: Int): Boolean = 
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