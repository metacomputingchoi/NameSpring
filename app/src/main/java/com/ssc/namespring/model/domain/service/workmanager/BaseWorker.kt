// model/domain/service/workmanager/BaseWorker.kt
package com.ssc.namespring.model.domain.service.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.ssc.namespring.model.domain.entity.TaskResult
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namespring.model.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker의 기본 구현을 제공하는 추상 클래스
 */
abstract class BaseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    protected val gson = Gson()
    protected val taskRepository = TaskRepository.getInstance(applicationContext)
    private val dataHelper = WorkerDataHelper(inputData)

    // 기존 속성들을 dataHelper로 위임
    protected val taskId: String get() = dataHelper.taskId
    protected val profileId: String get() = dataHelper.profileId
    protected val taskType get() = dataHelper.taskType
    protected fun getInputDataMap() = dataHelper.getInputDataMap()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            logInputData()
            updateTaskStatus(TaskStatus.RUNNING)
            setProgressAsync(Data.Builder().putInt("progress", 0).build())

            val result = performWork()
            logResult(result)

            saveResult(result)
            return@withContext createWorkerResult(result)
        } catch (e: Exception) {
            handleException(e)
            return@withContext createFailureResult(e)
        }
    }

    protected abstract suspend fun performWork(): WorkResult

    protected suspend fun updateProgress(progress: Int) {
        setProgressAsync(Data.Builder().putInt("progress", progress).build())
        taskRepository.updateTaskProgress(taskId, progress)
    }

    private fun logInputData() {
        Log.d("BaseWorker", "Starting work for task: $taskId, type: $taskType")
        Log.d("BaseWorker", "Input data: ${inputData.keyValueMap}")
    }

    private fun logResult(result: WorkResult) {
        Log.d("BaseWorker", "Work result - success: ${result.success}, error: ${result.error}")
    }

    private suspend fun updateTaskStatus(status: TaskStatus, error: String? = null) {
        taskRepository.updateTaskStatus(taskId, status, error)
    }

    private suspend fun saveResult(result: WorkResult) {
        val taskResult = TaskResult(
            taskId = taskId,
            taskType = taskType,
            success = result.success,
            data = result.data,
            rawData = result.rawData,
            error = result.error
        )
        taskRepository.saveTaskResult(taskResult)
        updateTaskStatus(
            if (result.success) TaskStatus.COMPLETED else TaskStatus.FAILED,
            result.error
        )
    }

    private fun createWorkerResult(result: WorkResult): Result {
        return if (result.success) {
            Result.success(
                Data.Builder()
                    .putString("task_id", taskId)
                    .putBoolean("success", true)
                    .build()
            )
        } else {
            Result.failure(
                Data.Builder()
                    .putString("task_id", taskId)
                    .putString("error", result.error ?: "Unknown error")
                    .build()
            )
        }
    }

    private suspend fun handleException(e: Exception) {
        Log.e("BaseWorker", "Worker failed with exception", e)
        try {
            updateTaskStatus(TaskStatus.FAILED, e.message)
        } catch (updateError: Exception) {
            Log.e("BaseWorker", "Failed to update task status", updateError)
        }
    }

    private fun createFailureResult(e: Exception): Result {
        return Result.failure(
            Data.Builder()
                .putString("task_id", taskId)
                .putString("error", e.message ?: "Unknown error")
                .build()
        )
    }
}