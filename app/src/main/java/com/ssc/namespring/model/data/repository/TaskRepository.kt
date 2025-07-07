// model/data/repository/TaskRepository.kt
package com.ssc.namespring.model.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.ssc.namespring.model.data.repository.task.*
import com.ssc.namespring.model.data.repository.task.interfaces.*
import com.ssc.namespring.model.domain.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * TaskRepository - 파사드 패턴을 사용하여 Task 관련 기능들을 통합 제공
 * 기존 외부 인터페이스를 유지하면서 내부 구현을 여러 전문 클래스로 위임
 */
class TaskRepository private constructor(private val context: Context) {

    companion object {
        private const val TAG = "TaskRepository"

        @Volatile
        private var INSTANCE: TaskRepository? = null

        fun getInstance(context: Context): TaskRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TaskRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val gson = Gson()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Services (기존과 동일)
    private val fileService: ITaskFileService = TaskFileService(context, gson)
    private val dataService: ITaskDataService = TaskDataService(fileService, repositoryScope)
    private val updateService: ITaskUpdateService = TaskUpdateService(dataService)
    private val resultService: ITaskResultService = TaskResultService(fileService)

    // Managers (새로 추가)
    private val taskManager = TaskManager(dataService, updateService, repositoryScope)
    private val taskResultManager = TaskResultManager(context, resultService, gson, repositoryScope)
    private val taskQueryService = TaskQueryService(dataService.taskHistoryMap)
    private val taskMaintenanceService = TaskMaintenanceService(fileService)

    // Public properties (기존과 동일한 인터페이스 유지)
    val taskHistoryMap: LiveData<Map<String, TaskHistory>> = dataService.taskHistoryMap

    // Task management (taskManager로 위임)
    fun saveTask(task: Task) = taskManager.saveTask(task)
    fun updateTask(task: Task) = taskManager.updateTask(task)
    fun updateTaskStatus(taskId: String, status: TaskStatus, errorMessage: String? = null) = 
        taskManager.updateTaskStatus(taskId, status, errorMessage)
    fun updateTaskProgress(taskId: String, progress: Int) = 
        taskManager.updateTaskProgress(taskId, progress)
    suspend fun getTask(taskId: String): Task? = taskManager.getTask(taskId)
    fun clearTaskHistory(profileId: String) = taskManager.clearTaskHistory(profileId)

    // Task result management (taskResultManager로 위임)
    fun saveTaskResult(result: TaskResult) = taskResultManager.saveTaskResult(result)
    fun getTaskResult(taskId: String): TaskResult? = taskResultManager.getTaskResult(taskId)
    suspend fun saveTaskResultWithFile(taskResult: TaskResult): TaskResult = 
        taskResultManager.saveTaskResultWithFile(taskResult)
    suspend fun loadRawDataFromFile(filePath: String): String? = 
        taskResultManager.loadRawDataFromFile(filePath)

    // Task queries (taskQueryService로 위임)
    fun getTaskHistory(profileId: String): List<Task> = taskQueryService.getTaskHistory(profileId)
    fun getTaskCount(profileId: String, type: TaskType? = null, status: TaskStatus? = null): Int = 
        taskQueryService.getTaskCount(profileId, type, status)
    fun getAllTasks(): List<Task> = taskQueryService.getAllTasks()

    // Task deletion (복합 작업)
    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        try {
            // Get task result to find raw data file
            val result = getTaskResult(taskId)

            // Delete task
            val deleted = taskManager.deleteTask(taskId)

            if (deleted) {
                // Delete raw data file if exists
                taskResultManager.deleteResultFile(result)
                Log.d(TAG, "Successfully deleted task: $taskId")
            } else {
                Log.e(TAG, "Failed to delete task: $taskId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task", e)
        }
    }

    // Maintenance (taskMaintenanceService로 위임)
    suspend fun cleanupOldTaskFiles(daysToKeep: Int = 7) = 
        taskMaintenanceService.cleanupOldTaskFiles(daysToKeep)
}