// model/data/repository/task/TaskMaintenanceService.kt
package com.ssc.namespring.model.data.repository.task

import android.util.Log
import com.ssc.namespring.model.data.repository.task.interfaces.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskMaintenanceService(
    private val fileService: ITaskFileService
) {
    companion object {
        private const val TAG = "TaskMaintenanceService"
    }

    suspend fun cleanupOldTaskFiles(daysToKeep: Int = 7): Boolean = withContext(Dispatchers.IO) {
        try {
            val cleaned = fileService.cleanupOldFiles(daysToKeep)
            if (!cleaned) {
                Log.e(TAG, "Failed to cleanup old task files")
            }
            cleaned
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old task files", e)
            false
        }
    }
}