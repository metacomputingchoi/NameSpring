// model/data/repository/task/helpers/TaskOperationsDelegate.kt
package com.ssc.namespring.model.data.repository.task.helpers

import android.util.Log
import com.ssc.namespring.model.data.repository.task.managers.TaskCRUDManager
import com.ssc.namespring.model.data.repository.task.managers.TaskLiveDataManager
import com.ssc.namespring.model.data.repository.task.managers.TaskPersistenceManager
import com.ssc.namespring.model.domain.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class TaskOperationsDelegate(
    private val liveDataManager: TaskLiveDataManager,
    private val persistenceManager: TaskPersistenceManager,
    private val crudManager: TaskCRUDManager
) {
    companion object {
        private const val TAG = "TaskOperationsDelegate"
    }

    suspend fun saveTask(task: Task): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentMap = liveDataManager.getCurrentMap()
            val history = currentMap[task.profileId] ?: TaskHistory(task.profileId, emptyList())
            val updatedTasks = crudManager.addOrUpdateTask(history, task)

            val saved = persistenceManager.saveTaskHistory(task.profileId, updatedTasks)
            if (saved) {
                val newHistory = TaskHistory(task.profileId, updatedTasks)
                liveDataManager.updateHistory(task.profileId, newHistory)
            }
            saved
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save task", e)
            false
        }
    }

    suspend fun deleteTask(taskId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentMap = liveDataManager.getCurrentMap()
            val taskInfo = crudManager.findTaskInHistories(currentMap, taskId)

            if (taskInfo != null) {
                val (profileId, _) = taskInfo
                val updatedTasks = crudManager.removeTask(
                    currentMap[profileId]!!.tasks, 
                    taskId
                )

                val saved = persistenceManager.saveTaskHistory(profileId, updatedTasks)
                if (saved) {
                    persistenceManager.deleteTaskFiles(taskId)
                    val newHistory = TaskHistory(profileId, updatedTasks)
                    liveDataManager.updateHistory(profileId, newHistory)
                    Log.d(TAG, "Deleted task: $taskId")
                    true
                } else {
                    false
                }
            } else {
                Log.w(TAG, "Task not found: $taskId")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete task", e)
            false
        }
    }

    suspend fun clearTaskHistory(profileId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val tasks = getTaskHistory(profileId)
            tasks.forEach { task ->
                persistenceManager.deleteTaskFiles(task.id)
            }
            persistenceManager.deleteTaskHistoryFile(profileId)
            liveDataManager.removeHistory(profileId)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear task history", e)
            false
        }
    }

    suspend fun getTask(taskId: String): Task? = withContext(Dispatchers.IO) {
        crudManager.getTask(liveDataManager.getCurrentMap(), taskId)
    }

    suspend fun getTaskHistory(profileId: String): List<Task> = 
        withContext(Dispatchers.IO) {
            liveDataManager.getCurrentMap()[profileId]?.tasks ?: emptyList()
        }

    suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        liveDataManager.getCurrentMap().values.flatMap { it.tasks }
    }

    suspend fun getTaskCount(
        profileId: String, 
        type: TaskType?, 
        status: TaskStatus?
    ): Int = withContext(Dispatchers.IO) {
        val tasks = getTaskHistory(profileId)
        crudManager.countTasks(tasks, type, status)
    }
}