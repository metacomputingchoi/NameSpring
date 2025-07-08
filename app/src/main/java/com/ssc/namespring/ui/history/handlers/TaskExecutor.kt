// ui/history/handlers/TaskExecutor.kt
package com.ssc.namespring.ui.history.handlers

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.ssc.namespring.HistoryActivity
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namespring.model.domain.service.workmanager.TaskWorkManager
import com.ssc.namespring.model.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class TaskExecutor(
    private val context: Context,
    private val taskWorkManager: TaskWorkManager,
    private val taskRepository: TaskRepository
) {
    fun cancelTask(task: Task) {
        (context as? HistoryActivity)?.lifecycleScope?.launch {
            try {
                taskWorkManager.cancelTask(task.id)
                withContext(Dispatchers.IO) {
                    kotlinx.coroutines.delay(500)
                }
                showToast("작업이 취소되었습니다")
            } catch (e: Exception) {
                showToast("작업 취소 중 오류가 발생했습니다")
            }
        }
    }

    fun retryTask(task: Task) {
        val newTask = task.copy(
            id = UUID.randomUUID().toString(),
            status = TaskStatus.PENDING,
            progress = 0,
            errorMessage = null,
            createdAt = System.currentTimeMillis(),
            startedAt = null,
            completedAt = null,
            workerId = null
        )

        taskWorkManager.enqueueTask(newTask)
        showToast("작업을 다시 시작했습니다")
    }

    fun deleteTasks(tasks: List<Task>, onComplete: () -> Unit) {
        val progressDialog = createProgressDialog()

        (context as? HistoryActivity)?.lifecycleScope?.launch {
            try {
                performDeletion(tasks)
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    onComplete()
                    showToast("${'$'}{tasks.size}개의 작업이 삭제되었습니다")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    showToast("삭제 중 오류가 발생했습니다")
                }
            }
        }
    }

    private suspend fun performDeletion(tasks: List<Task>) {
        tasks.forEach { task ->
            when (task.status) {
                TaskStatus.PENDING, TaskStatus.RUNNING -> {
                    taskWorkManager.cancelTask(task.id)
                    withContext(Dispatchers.IO) {
                        kotlinx.coroutines.delay(100)
                        taskRepository.deleteTask(task.id)
                    }
                }
                else -> {
                    withContext(Dispatchers.IO) {
                        taskRepository.deleteTask(task.id)
                    }
                }
            }
        }
    }

    private fun createProgressDialog() = ProgressDialog(context).apply {
        setMessage("삭제 중...")
        setCancelable(false)
        show()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}