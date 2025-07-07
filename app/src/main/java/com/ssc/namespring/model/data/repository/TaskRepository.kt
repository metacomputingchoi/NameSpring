// model/data/repository/TaskRepository.kt
package com.ssc.namespring.model.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.domain.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class TaskRepository private constructor(private val context: Context) {

    companion object {
        private const val TASK_FILE_PREFIX = "tasks_"
        private const val TASK_RESULT_PREFIX = "results_"

        @Volatile
        private var INSTANCE: TaskRepository? = null

        fun getInstance(context: Context): TaskRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TaskRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val gson = Gson()
    private val tasksDir = File(context.filesDir, "tasks").apply { mkdirs() }

    private val _taskHistoryMap = MutableLiveData<Map<String, TaskHistory>>()
    val taskHistoryMap: LiveData<Map<String, TaskHistory>> = _taskHistoryMap

    init {
        loadAllTaskHistories()
    }

    fun saveTask(task: Task) {
        val history = getTaskHistory(task.profileId).toMutableList()
        val existingIndex = history.indexOfFirst { it.id == task.id }

        if (existingIndex >= 0) {
            history[existingIndex] = task
        } else {
            history.add(task)
        }

        saveTaskHistory(task.profileId, history)
    }

    fun updateTask(task: Task) {
        saveTask(task)
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus, errorMessage: String? = null) {
        val allHistories = _taskHistoryMap.value ?: emptyMap()

        for ((profileId, history) in allHistories) {
            val taskIndex = history.tasks.indexOfFirst { it.id == taskId }
            if (taskIndex >= 0) {
                val task = history.tasks[taskIndex]
                val updatedTask = task.copy(
                    status = status,
                    errorMessage = errorMessage,
                    startedAt = if (status == TaskStatus.RUNNING && task.startedAt == null) {
                        System.currentTimeMillis()
                    } else task.startedAt,
                    completedAt = if (status in listOf(TaskStatus.COMPLETED, TaskStatus.FAILED, TaskStatus.CANCELLED)) {
                        System.currentTimeMillis()
                    } else task.completedAt
                )
                saveTask(updatedTask)
                break
            }
        }
    }

    fun updateTaskProgress(taskId: String, progress: Int) {
        val allHistories = _taskHistoryMap.value ?: emptyMap()

        for ((profileId, history) in allHistories) {
            val taskIndex = history.tasks.indexOfFirst { it.id == taskId }
            if (taskIndex >= 0) {
                val task = history.tasks[taskIndex]
                val updatedTask = task.copy(progress = progress)
                saveTask(updatedTask)
                break
            }
        }
    }

    fun getTask(taskId: String): Task? {
        val allHistories = _taskHistoryMap.value ?: emptyMap()

        for ((_, history) in allHistories) {
            val task = history.tasks.find { it.id == taskId }
            if (task != null) return task
        }

        return null
    }

    fun getTaskHistory(profileId: String): List<Task> {
        return _taskHistoryMap.value?.get(profileId)?.tasks ?: emptyList()
    }

    fun saveTaskResult(result: TaskResult) {
        val file = File(tasksDir, "${TASK_RESULT_PREFIX}${result.taskId}.json")
        file.writeText(gson.toJson(result))
    }

    fun getTaskResult(taskId: String): TaskResult? {
        val file = File(tasksDir, "${TASK_RESULT_PREFIX}${taskId}.json")
        return if (file.exists()) {
            gson.fromJson(file.readText(), TaskResult::class.java)
        } else null
    }

    private fun saveTaskHistory(profileId: String, tasks: List<Task>) {
        val file = File(tasksDir, "${TASK_FILE_PREFIX}${profileId}.json")
        val history = TaskHistory(profileId, tasks)
        file.writeText(gson.toJson(history))

        // Update LiveData
        val currentMap = _taskHistoryMap.value?.toMutableMap() ?: mutableMapOf()
        currentMap[profileId] = history
        _taskHistoryMap.postValue(currentMap)
    }

    private fun loadAllTaskHistories() {
        val historyMap = mutableMapOf<String, TaskHistory>()

        tasksDir.listFiles { file -> file.name.startsWith(TASK_FILE_PREFIX) }?.forEach { file ->
            try {
                val history = gson.fromJson(file.readText(), TaskHistory::class.java)
                historyMap[history.profileId] = history
            } catch (e: Exception) {
                // Handle error
            }
        }

        _taskHistoryMap.postValue(historyMap)
    }

    fun clearTaskHistory(profileId: String) {
        val file = File(tasksDir, "${TASK_FILE_PREFIX}${profileId}.json")
        if (file.exists()) {
            file.delete()
        }

        val currentMap = _taskHistoryMap.value?.toMutableMap() ?: mutableMapOf()
        currentMap.remove(profileId)
        _taskHistoryMap.postValue(currentMap)
    }

    // 대용량 데이터를 파일로 저장
    suspend fun saveTaskResultWithFile(taskResult: TaskResult): TaskResult = withContext(Dispatchers.IO) {
        try {
            // rawData가 크면 파일로 저장
            val modifiedResult = if (taskResult.rawData != null && taskResult.rawData.length > 5000) {
                val filePath = saveRawDataToFile(taskResult.taskId, taskResult.rawData)
                taskResult.copy(
                    rawData = null,  // 메모리에서 제거
                    data = (taskResult.data ?: emptyMap()) + mapOf("rawDataFile" to filePath)
                )
            } else {
                taskResult
            }

            // DB에 저장 (파일 경로 포함)
            saveTaskResult(modifiedResult)
            modifiedResult
        } catch (e: Exception) {
            Log.e("TaskRepository", "Failed to save task result with file", e)
            throw e
        }
    }

    // 파일에 저장
    private fun saveRawDataToFile(taskId: String, data: String): String {
        val fileName = "task_${taskId}_${System.currentTimeMillis()}.json"
        val file = File(context.filesDir, "task_results").apply {
            if (!exists()) mkdirs()
        }
        val targetFile = File(file, fileName)
        targetFile.writeText(data)
        return targetFile.absolutePath
    }

    // 파일에서 읽기
    suspend fun loadRawDataFromFile(filePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Failed to load raw data from file", e)
            null
        }
    }

    // 오래된 파일 정리
    suspend fun cleanupOldTaskFiles(daysToKeep: Int = 7) = withContext(Dispatchers.IO) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        val taskDir = File(context.filesDir, "task_results")

        taskDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                file.delete()
            }
        }
    }
}