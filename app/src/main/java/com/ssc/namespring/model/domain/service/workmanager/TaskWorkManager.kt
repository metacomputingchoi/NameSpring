// model/domain/service/workmanager/TaskWorkManager.kt
package com.ssc.namespring.model.domain.service.workmanager

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.ssc.namespring.model.domain.entity.*
import com.ssc.namespring.model.domain.service.workmanager.helpers.*
import com.ssc.namespring.model.domain.service.workmanager.interfaces.ITaskWorkManager
import com.ssc.namespring.model.data.repository.TaskRepository

class TaskWorkManager private constructor(
    private val context: Context
) : ITaskWorkManager {

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
    private val mainHandler = Handler(Looper.getMainLooper())
    private val taskRepository = TaskRepository.getInstance(context)

    // Helper classes
    private val workRequestBuilder = WorkRequestBuilder()
    private val statusMapper = TaskStatusMapper()
    private val workObserver = WorkObserver(workManager, taskRepository, statusMapper)

    private val _activeTasks = MutableLiveData<List<Task>>()
    override val activeTasks: LiveData<List<Task>> = _activeTasks

    private val _taskHistory = MutableLiveData<Map<String, TaskHistory>>()
    override val taskHistory: LiveData<Map<String, TaskHistory>> = _taskHistory

    init {
        mainHandler.post {
            initializeObservers()
        }
    }

    override fun enqueueTask(task: Task): String {
        val workRequest = workRequestBuilder.buildWorkRequest(task)

        workManager.enqueueUniqueWork(
            task.id,
            ExistingWorkPolicy.KEEP,
            workRequest
        )

        val updatedTask = task.copy(workerId = workRequest.id.toString())
        taskRepository.saveTask(updatedTask)

        workObserver.observeWorkInfo(workRequest.id, task.id)

        return task.id
    }

    override fun cancelTask(taskId: String) {
        workManager.cancelUniqueWork(taskId)
        taskRepository.updateTaskStatus(taskId, TaskStatus.CANCELLED)
    }

    override fun cancelTasksByProfile(profileId: String) {
        workManager.cancelAllWorkByTag("profile_$profileId")
    }

    override fun getTaskProgress(taskId: String): LiveData<WorkInfo?> {
        return workObserver.run {
            workManager.getWorkInfosForUniqueWorkLiveData(taskId)
                .map { workInfos -> workInfos.firstOrNull() }
        }
    }

    private fun initializeObservers() {
        workObserver.observeAllWork { workInfos ->
            updateActiveTasksFromWorkInfo(workInfos)
        }
        loadTaskHistory()
    }

    private fun loadTaskHistory() {
        _taskHistory.value = taskRepository.taskHistoryMap.value ?: emptyMap()

        taskRepository.taskHistoryMap.observeForever { historyMap ->
            _taskHistory.value = historyMap
        }
    }

    private fun updateActiveTasksFromWorkInfo(workInfos: List<WorkInfo>) {
        workInfos.forEach { workInfo ->
            statusMapper.extractTaskIdFromTags(workInfo.tags)?.let { taskId ->
                Log.d(TAG, "Updating task $taskId to state: ${workInfo.state}")
                statusMapper.mapWorkInfoToTaskStatus(workInfo.state)?.let { status ->
                    taskRepository.updateTaskStatus(taskId, status)
                }
            }
        }
    }
}