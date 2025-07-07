package com.ssc.namespring.model.data.repository.task

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.data.repository.task.interfaces.ITaskDataService
import com.ssc.namespring.model.data.repository.task.interfaces.ITaskFileService
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

    private val _taskHistoryMap = MutableLiveData<Map<String, TaskHistory>>()
    override val taskHistoryMap: LiveData<Map<String, TaskHistory>> = _taskHistoryMap

    private val dataLock = Mutex()

    init {
        loadAllHistories()
    }

    private fun loadAllHistories() {
        scope.launch {
            try {
                val histories = fileService.loadAllTaskHistories()
                _taskHistoryMap.postValue(histories)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load all histories", e)
                _taskHistoryMap.postValue(emptyMap())
            }
        }
    }

    override suspend fun saveTask(task: Task): Boolean = withContext(Dispatchers.IO) {
        dataLock.withLock {
            try {
                val currentMap = _taskHistoryMap.value ?: emptyMap()
                val history = currentMap[task.profileId]?.tasks ?: emptyList()
                val updatedTasks = history.toMutableList()

                val existingIndex = updatedTasks.indexOfFirst { it.id == task.id }
                if (existingIndex >= 0) {
                    updatedTasks[existingIndex] = task
                } else {
                    updatedTasks.add(task)
                }

                // Save to file
                val saved = fileService.saveTaskHistory(task.profileId, updatedTasks)

                if (saved) {
                    // Update LiveData
                    val newHistory = TaskHistory(task.profileId, updatedTasks)
                    val newMap = currentMap.toMutableMap()
                    newMap[task.profileId] = newHistory
                    _taskHistoryMap.postValue(newMap)
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
                val currentMap = _taskHistoryMap.value ?: emptyMap()

                // Find the task
                var profileId: String? = null
                var taskToDelete: Task? = null

                for ((pid, history) in currentMap) {
                    val task = history.tasks.find { it.id == taskId }
                    if (task != null) {
                        profileId = pid
                        taskToDelete = task
                        break
                    }
                }

                if (profileId != null && taskToDelete != null) {
                    // Remove from list
                    val updatedTasks = currentMap[profileId]!!.tasks
                        .filter { it.id != taskId }

                    // Save to file
                    val saved = fileService.saveTaskHistory(profileId, updatedTasks)

                    if (saved) {
                        // Delete associated files
                        fileService.deleteTaskFiles(taskId)

                        // Update LiveData
                        val newHistory = TaskHistory(profileId, updatedTasks)
                        val newMap = currentMap.toMutableMap()
                        newMap[profileId] = newHistory
                        _taskHistoryMap.postValue(newMap)

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
        _taskHistoryMap.value?.values?.flatMap { it.tasks }?.find { it.id == taskId }
    }

    override suspend fun getTaskHistory(profileId: String): List<Task> = 
        withContext(Dispatchers.IO) {
            _taskHistoryMap.value?.get(profileId)?.tasks ?: emptyList()
        }

    override suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        _taskHistoryMap.value?.values?.flatMap { it.tasks } ?: emptyList()
    }

    override suspend fun clearTaskHistory(profileId: String): Boolean = 
        withContext(Dispatchers.IO) {
            dataLock.withLock {
                try {
                    // Delete all task result files
                    val tasks = getTaskHistory(profileId)
                    tasks.forEach { task ->
                        fileService.deleteTaskFiles(task.id)
                    }

                    // Delete history file
                    fileService.deleteTaskHistoryFile(profileId)

                    // Update LiveData
                    val currentMap = _taskHistoryMap.value?.toMutableMap() ?: mutableMapOf()
                    currentMap.remove(profileId)
                    _taskHistoryMap.postValue(currentMap)

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
        tasks.count { task ->
            (type == null || task.type == type) &&
            (status == null || task.status == status)
        }
    }
}