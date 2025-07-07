// model/data/repository/task/TaskUpdateService.kt
package com.ssc.namespring.model.data.repository.task

import android.util.Log
import com.ssc.namespring.model.data.repository.task.interfaces.ITaskDataService
import com.ssc.namespring.model.data.repository.task.interfaces.ITaskUpdateService
import com.ssc.namespring.model.domain.entity.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskUpdateService(
    private val dataService: ITaskDataService
) : ITaskUpdateService {

    companion object {
        private const val TAG = "TaskUpdateService"
    }

    override suspend fun updateTaskStatus(
        taskId: String, 
        status: TaskStatus, 
        errorMessage: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val task = dataService.getTask(taskId)

            if (task != null) {
                val updatedTask = task.copy(
                    status = status,
                    errorMessage = errorMessage,
                    startedAt = if (status == TaskStatus.RUNNING && task.startedAt == null) {
                        System.currentTimeMillis()
                    } else task.startedAt,
                    completedAt = if (status in listOf(
                            TaskStatus.COMPLETED, 
                            TaskStatus.FAILED, 
                            TaskStatus.CANCELLED
                        )) {
                        System.currentTimeMillis()
                    } else task.completedAt
                )

                val result = dataService.updateTask(updatedTask)
                if (result) {
                    Log.d(TAG, "Updated task $taskId to status: $status")
                }
                result
            } else {
                Log.w(TAG, "Task not found for status update: $taskId")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update task status", e)
            false
        }
    }

    override suspend fun updateTaskProgress(taskId: String, progress: Int): Boolean = 
        withContext(Dispatchers.IO) {
            try {
                val task = dataService.getTask(taskId)

                if (task != null) {
                    val updatedTask = task.copy(progress = progress)
                    val result = dataService.updateTask(updatedTask)
                    if (result) {
                        Log.d(TAG, "Updated task $taskId progress: $progress%")
                    }
                    result
                } else {
                    Log.w(TAG, "Task not found for progress update: $taskId")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update task progress", e)
                false
            }
        }
}