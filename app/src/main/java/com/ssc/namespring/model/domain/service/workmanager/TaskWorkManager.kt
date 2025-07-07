// model/domain/service/workmanager/TaskWorkManager.kt
package com.ssc.namespring.model.domain.service.workmanager

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.google.gson.Gson
import com.ssc.namespring.model.domain.entity.*
import com.ssc.namespring.model.domain.service.workmanager.workers.NamingWorker
import com.ssc.namespring.model.domain.service.workmanager.workers.EvaluationWorker
import com.ssc.namespring.model.domain.service.workmanager.workers.ComparisonWorker
import com.ssc.namespring.model.domain.service.workmanager.workers.ReportGenerationWorker
import com.ssc.namespring.model.data.repository.TaskRepository
import java.util.concurrent.TimeUnit

class TaskWorkManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "TaskWorkManager"

        @Volatile
        private var INSTANCE: TaskWorkManager? = null

        fun getInstance(context: Context): TaskWorkManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TaskWorkManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val workManager = WorkManager.getInstance(context)
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val taskRepository = TaskRepository.getInstance(context)

    private val _activeTasks = MutableLiveData<List<Task>>()
    val activeTasks: LiveData<List<Task>> = _activeTasks

    private val _taskHistory = MutableLiveData<Map<String, TaskHistory>>()
    val taskHistory: LiveData<Map<String, TaskHistory>> = _taskHistory

    init {
        // LiveData 관찰은 메인 스레드에서 실행
        mainHandler.post {
            observeAllWork()
            loadTaskHistory()
        }
    }

    fun enqueueTask(task: Task): String {
        val inputData = Data.Builder()
            .putString("task_id", task.id)
            .putString("profile_id", task.profileId)
            .putString("task_type", task.type.name)
            .putString("input_data", gson.toJson(task.inputData))
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = when (task.type) {
            TaskType.NAMING -> OneTimeWorkRequestBuilder<NamingWorker>()
            TaskType.EVALUATION -> OneTimeWorkRequestBuilder<EvaluationWorker>()
            TaskType.COMPARISON -> OneTimeWorkRequestBuilder<ComparisonWorker>()
            TaskType.REPORT_GENERATION -> OneTimeWorkRequestBuilder<ReportGenerationWorker>()
        }
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag("profile_${task.profileId}")
            .addTag("type_${task.type.name}")
            .addTag("task_${task.id}")  // task_id 태그 추가
            .build()

        workManager.enqueueUniqueWork(
            task.id,
            ExistingWorkPolicy.KEEP,
            workRequest
        )

        // Task 저장 및 상태 업데이트
        val updatedTask = task.copy(workerId = workRequest.id.toString())
        taskRepository.saveTask(updatedTask)

        // WorkInfo 상태 관찰
        workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever { workInfo ->
            workInfo?.let {
                Log.d(TAG, "Work ${workRequest.id} state changed to: ${it.state}")
                updateTaskStatusFromWorkInfo(task.id, it)
            }
        }

        return task.id
    }

    fun cancelTask(taskId: String) {
        workManager.cancelUniqueWork(taskId)
        updateTaskStatus(taskId, TaskStatus.CANCELLED)
    }

    fun cancelTasksByProfile(profileId: String) {
        workManager.cancelAllWorkByTag("profile_$profileId")
    }

    fun getTaskProgress(taskId: String): LiveData<WorkInfo?> {
        return workManager.getWorkInfosForUniqueWorkLiveData(taskId)
            .map { workInfos -> workInfos.firstOrNull() }
    }

    private fun observeAllWork() {
        // 모든 상태의 작업을 관찰
        val workQuery = WorkQuery.Builder
            .fromStates(listOf(
                WorkInfo.State.ENQUEUED,
                WorkInfo.State.RUNNING,
                WorkInfo.State.SUCCEEDED,
                WorkInfo.State.FAILED,
                WorkInfo.State.CANCELLED
            ))
            .build()

        workManager.getWorkInfosLiveData(workQuery).observeForever { workInfos ->
            Log.d(TAG, "Observed ${workInfos.size} work infos")
            updateActiveTasksFromWorkInfo(workInfos)
        }
    }

    private fun loadTaskHistory() {
        // Load task history from repository
        _taskHistory.value = taskRepository.taskHistoryMap.value ?: emptyMap()

        // Observe repository changes
        taskRepository.taskHistoryMap.observeForever { historyMap ->
            _taskHistory.value = historyMap
        }
    }

    private fun updateTaskStatus(taskId: String, status: TaskStatus) {
        Log.d(TAG, "Updating task $taskId to status: $status")
        taskRepository.updateTaskStatus(taskId, status)
    }

    private fun updateTaskStatusFromWorkInfo(taskId: String, workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.RUNNING -> {
                updateTaskStatus(taskId, TaskStatus.RUNNING)
            }
            WorkInfo.State.SUCCEEDED -> {
                updateTaskStatus(taskId, TaskStatus.COMPLETED)
            }
            WorkInfo.State.FAILED -> {
                updateTaskStatus(taskId, TaskStatus.FAILED)
            }
            WorkInfo.State.CANCELLED -> {
                updateTaskStatus(taskId, TaskStatus.CANCELLED)
            }
            else -> {
                // ENQUEUED, BLOCKED - 대기 상태
            }
        }
    }

    private fun updateActiveTasksFromWorkInfo(workInfos: List<WorkInfo>) {
        workInfos.forEach { workInfo ->
            // 태그에서 task_id 추출
            val taskId = workInfo.tags.find { it.startsWith("task_") }
                ?.removePrefix("task_")

            if (taskId != null) {
                Log.d(TAG, "Updating task $taskId to state: ${workInfo.state}")
                updateTaskStatusFromWorkInfo(taskId, workInfo)
            }
        }
    }
}

// Extension function for LiveData mapping
private fun <T, R> LiveData<T>.map(transform: (T) -> R): LiveData<R> {
    val result = MutableLiveData<R>()
    this.observeForever { value ->
        result.value = transform(value)
    }
    return result
}