// model/data/repository/TaskRepository.kt
package com.ssc.namespring.model.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.domain.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class TaskRepository private constructor(private val context: Context) {

    companion object {
        private const val TAG = "TaskRepository"
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
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _taskHistoryMap = MutableLiveData<Map<String, TaskHistory>>()
    val taskHistoryMap: LiveData<Map<String, TaskHistory>> = _taskHistoryMap

    init {
        loadAllTaskHistories()
    }

    fun saveTask(task: Task) {
        repositoryScope.launch {
            try {
                val history = getTaskHistory(task.profileId).toMutableList()
                val existingIndex = history.indexOfFirst { it.id == task.id }

                if (existingIndex >= 0) {
                    history[existingIndex] = task
                } else {
                    history.add(task)
                }

                saveTaskHistory(task.profileId, history)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save task", e)
            }
        }
    }

    fun updateTask(task: Task) {
        saveTask(task)
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus, errorMessage: String? = null) {
        repositoryScope.launch {
            try {
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
                        Log.d(TAG, "Updated task $taskId to status: $status")
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update task status", e)
            }
        }
    }

    fun updateTaskProgress(taskId: String, progress: Int) {
        repositoryScope.launch {
            try {
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
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update task progress", e)
            }
        }
    }

    suspend fun getTask(taskId: String): Task? {
        return withContext(Dispatchers.IO) {
            _taskHistoryMap.value?.values?.flatMap { it.tasks }?.find { it.id == taskId }
        }
    }

    fun getTaskHistory(profileId: String): List<Task> {
        return _taskHistoryMap.value?.get(profileId)?.tasks ?: emptyList()
    }

    fun saveTaskResult(result: TaskResult) {
        repositoryScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val file = File(tasksDir, "${TASK_RESULT_PREFIX}${result.taskId}.json")
                    file.writeText(gson.toJson(result))
                    Log.d(TAG, "Saved task result for ${result.taskId}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save task result", e)
            }
        }
    }

    fun getTaskResult(taskId: String): TaskResult? {
        return try {
            val file = File(tasksDir, "${TASK_RESULT_PREFIX}${taskId}.json")
            if (file.exists()) {
                gson.fromJson(file.readText(), TaskResult::class.java)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load task result", e)
            null
        }
    }

    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        try {
            // Find and remove task from history
            val allHistories = _taskHistoryMap.value ?: emptyMap()

            for ((profileId, history) in allHistories) {
                val tasks = history.tasks.toMutableList()
                val removed = tasks.removeAll { it.id == taskId }

                if (removed) {
                    // Save updated history
                    saveTaskHistory(profileId, tasks)

                    // Delete associated result file
                    val resultFile = File(tasksDir, "${TASK_RESULT_PREFIX}${taskId}.json")
                    if (resultFile.exists()) {
                        resultFile.delete()
                    }

                    // Delete raw data file if exists
                    val result = getTaskResult(taskId)
                    result?.data?.get("rawDataFile")?.let { filePath ->
                        File(filePath.toString()).delete()
                    }

                    Log.d(TAG, "Deleted task: $taskId")
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete task", e)
        }
    }

    private fun saveTaskHistory(profileId: String, tasks: List<Task>) {
        try {
            val file = File(tasksDir, "${TASK_FILE_PREFIX}${profileId}.json")
            val history = TaskHistory(profileId, tasks)
            file.writeText(gson.toJson(history))

            // Update LiveData
            val currentMap = _taskHistoryMap.value?.toMutableMap() ?: mutableMapOf()
            currentMap[profileId] = history
            _taskHistoryMap.postValue(currentMap)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save task history", e)
        }
    }

    private fun loadAllTaskHistories() {
        repositoryScope.launch {
            try {
                val historyMap = mutableMapOf<String, TaskHistory>()

                tasksDir.listFiles { file -> file.name.startsWith(TASK_FILE_PREFIX) }?.forEach { file ->
                    try {
                        val history = gson.fromJson(file.readText(), TaskHistory::class.java)
                        historyMap[history.profileId] = history
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load task history from ${file.name}", e)
                    }
                }

                _taskHistoryMap.postValue(historyMap)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load task histories", e)
            }
        }
    }

    fun clearTaskHistory(profileId: String) {
        repositoryScope.launch {
            try {
                // Delete all task results for this profile
                val history = getTaskHistory(profileId)
                history.forEach { task ->
                    val resultFile = File(tasksDir, "${TASK_RESULT_PREFIX}${task.id}.json")
                    if (resultFile.exists()) {
                        resultFile.delete()
                    }
                }

                // Delete task history file
                val file = File(tasksDir, "${TASK_FILE_PREFIX}${profileId}.json")
                if (file.exists()) {
                    file.delete()
                }

                // Update LiveData
                val currentMap = _taskHistoryMap.value?.toMutableMap() ?: mutableMapOf()
                currentMap.remove(profileId)
                _taskHistoryMap.postValue(currentMap)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear task history", e)
            }
        }
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

            // 결과 저장 (파일 경로 포함)
            val file = File(tasksDir, "${TASK_RESULT_PREFIX}${modifiedResult.taskId}.json")
            file.writeText(gson.toJson(modifiedResult))

            modifiedResult
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save task result with file", e)
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
            Log.e(TAG, "Failed to load raw data from file", e)
            null
        }
    }

    // 오래된 파일 정리
    suspend fun cleanupOldTaskFiles(daysToKeep: Int = 7) = withContext(Dispatchers.IO) {
        try {
            val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)

            // Clean up task result files
            val taskResultsDir = File(context.filesDir, "task_results")
            taskResultsDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    file.delete()
                }
            }

            // Clean up old task history files
            tasksDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime && file.name.startsWith(TASK_RESULT_PREFIX)) {
                    file.delete()
                }
            }

            Log.d(TAG, "Cleaned up old task files")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup old task files", e)
        }
    }

    // 특정 프로필의 작업 수 가져오기
    fun getTaskCount(profileId: String, type: TaskType? = null, status: TaskStatus? = null): Int {
        val tasks = getTaskHistory(profileId)
        return tasks.count { task ->
            (type == null || task.type == type) &&
                    (status == null || task.status == status)
        }
    }

    // 모든 작업 가져오기
    fun getAllTasks(): List<Task> {
        val allHistories = _taskHistoryMap.value ?: emptyMap()
        return allHistories.values.flatMap { it.tasks }
    }
}