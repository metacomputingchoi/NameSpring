// model/data/repository/task/TaskDataService.kt
package com.ssc.namespring.model.data.repository.task

import android.util.Log
import androidx.lifecycle.LiveData
import com.ssc.namespring.model.data.repository.task.interfaces.ITaskDataService
import com.ssc.namespring.model.data.repository.task.interfaces.ITaskFileService
import com.ssc.namespring.model.data.repository.task.managers.TaskCRUDManager
import com.ssc.namespring.model.data.repository.task.managers.TaskLiveDataManager
import com.ssc.namespring.model.data.repository.task.managers.TaskPersistenceManager
import com.ssc.namespring.model.domain.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TaskDataService(
    private val fileService: ITaskFileService,
    private val scope: CoroutineScope
) : ITaskDataService {

    companion object {
        private const val TAG = "TaskDataService"
    }

    private val liveDataManager = TaskLiveDataManager()
    private val persistenceManager = TaskPersistenceManager(fileService)
    private val crudManager = TaskCRUDManager()
    private val dataLock = Mutex()

    override val taskHistoryMap: LiveData<Map<String, TaskHistory>> = 
        liveDataManager.taskHistoryMap

    init {
        loadAllHistories()
    }

    private fun loadAllHistories() {
        scope.launch {
            val histories = persistenceManager.loadAllHistories()
            liveDataManager.updateMap(histories)
        }
    }

    override suspend fun saveTask(task: Task): Boolean = withContext(Dispatchers.IO) {
        dataLock.withLock {
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
    }

    override suspend fun updateTask(task: Task): Boolean = saveTask(task)

    override suspend fun deleteTask(taskId: String): Boolean = withContext(Dispatchers.IO) {
        dataLock.withLock {
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
    }

    override suspend fun getTask(taskId: String): Task? = withContext(Dispatchers.IO) {
        crudManager.getTask(liveDataManager.getCurrentMap(), taskId)
    }

    override suspend fun getTaskHistory(profileId: String): List<Task> = 
        withContext(Dispatchers.IO) {
            liveDataManager.getCurrentMap()[profileId]?.tasks ?: emptyList()
        }

    override suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        liveDataManager.getCurrentMap().values.flatMap { it.tasks }
    }

    override suspend fun clearTaskHistory(profileId: String): Boolean = 
        withContext(Dispatchers.IO) {
            dataLock.withLock {
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
        }

    override suspend fun getTaskCount(
        profileId: String, 
        type: TaskType?, 
        status: TaskStatus?
    ): Int = withContext(Dispatchers.IO) {
        val tasks = getTaskHistory(profileId)
        crudManager.countTasks(tasks, type, status)
    }
}