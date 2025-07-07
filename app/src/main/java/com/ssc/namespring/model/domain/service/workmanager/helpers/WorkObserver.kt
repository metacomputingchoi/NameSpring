// model/domain/service/workmanager/helpers/WorkObserver.kt
package com.ssc.namespring.model.domain.service.workmanager.helpers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.ssc.namespring.model.data.repository.TaskRepository

internal class WorkObserver(
    private val workManager: WorkManager,
    private val taskRepository: TaskRepository,
    private val statusMapper: TaskStatusMapper
) {
    companion object {
        private const val TAG = "WorkObserver"
    }

    fun observeWorkInfo(workRequestId: java.util.UUID, taskId: String) {
        workManager.getWorkInfoByIdLiveData(workRequestId).observeForever { workInfo ->
            workInfo?.let {
                Log.d(TAG, "Work $workRequestId state changed to: ${it.state}")
                updateTaskStatus(taskId, it)
            }
        }
    }

    fun observeAllWork(onWorkInfosUpdated: (List<WorkInfo>) -> Unit) {
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
            onWorkInfosUpdated(workInfos)
        }
    }

    private fun updateTaskStatus(taskId: String, workInfo: WorkInfo) {
        statusMapper.mapWorkInfoToTaskStatus(workInfo.state)?.let { status ->
            Log.d(TAG, "Updating task $taskId to status: $status")
            taskRepository.updateTaskStatus(taskId, status)
        }
    }

    fun <T, R> LiveData<T>.map(transform: (T) -> R): LiveData<R> {
        val result = MutableLiveData<R>()
        this.observeForever { value ->
            result.value = transform(value)
        }
        return result
    }
}