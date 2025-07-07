// model/domain/service/workmanager/BaseWorker.kt
package com.ssc.namespring.model.domain.service.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.domain.entity.TaskResult
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.model.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    protected val gson = Gson()
    protected val taskRepository = TaskRepository.getInstance(applicationContext)

    protected val taskId: String
        get() = inputData.getString("task_id") ?: ""

    protected val profileId: String
        get() = inputData.getString("profile_id") ?: ""

    protected val taskType: TaskType
        get() = TaskType.valueOf(inputData.getString("task_type") ?: TaskType.EVALUATION.name)

    protected fun getInputDataMap(): Map<String, Any> {
        val jsonString = inputData.getString("input_data") ?: "{}"
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Log input data for debugging
            Log.d("BaseWorker", "Starting work for task: $taskId, type: $taskType")
            Log.d("BaseWorker", "Input data: ${inputData.keyValueMap}")

            // Update task status to RUNNING
            taskRepository.updateTaskStatus(taskId, TaskStatus.RUNNING)
            setProgressAsync(Data.Builder().putInt("progress", 0).build())

            // Execute the actual work
            val result = performWork()

            // Log result
            Log.d("BaseWorker", "Work result - success: ${result.success}, error: ${result.error}")

            // Save result to repository
            val taskResult = TaskResult(
                taskId = taskId,
                taskType = taskType,
                success = result.success,
                data = result.data,
                rawData = result.rawData,
                error = result.error
            )

            // 먼저 Repository에 저장
            taskRepository.saveTaskResult(taskResult)
            taskRepository.updateTaskStatus(
                taskId,
                if (result.success) TaskStatus.COMPLETED else TaskStatus.FAILED,
                result.error
            )

            // WorkManager Result는 정말 최소한의 데이터만
            if (result.success) {
                Result.success(
                    Data.Builder()
                        .putString("task_id", taskId)
                        .putBoolean("success", true)
                        .build()  // 다른 데이터 제거
                )
            } else {
                Result.failure(
                    Data.Builder()
                        .putString("task_id", taskId)
                        .putString("error", result.error ?: "Unknown error")
                        .build()
                )
            }
        } catch (e: Exception) {
            Log.e("BaseWorker", "Worker failed with exception", e)

            // 예외 발생 시에도 상태 업데이트
            try {
                taskRepository.updateTaskStatus(taskId, TaskStatus.FAILED, e.message)
            } catch (updateError: Exception) {
                Log.e("BaseWorker", "Failed to update task status", updateError)
            }

            Result.failure(
                Data.Builder()
                    .putString("task_id", taskId)
                    .putString("error", e.message ?: "Unknown error")
                    .build()
            )
        }
    }

    protected abstract suspend fun performWork(): WorkResult

    protected suspend fun updateProgress(progress: Int) {
        setProgressAsync(
            Data.Builder()
                .putInt("progress", progress)
                .build()
        )
        taskRepository.updateTaskProgress(taskId, progress)
    }

    data class WorkResult(
        val success: Boolean,
        val data: Map<String, Any>? = null,
        val rawData: String? = null,  // 대용량 데이터용
        val error: String? = null
    )
}