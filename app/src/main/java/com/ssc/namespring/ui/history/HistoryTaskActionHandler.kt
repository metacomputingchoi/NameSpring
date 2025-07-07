// app/src/main/java/com/ssc/namespring/ui/history/HistoryTaskActionHandler.kt
package com.ssc.namespring.ui.history

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

class HistoryTaskActionHandler(
    private val context: Context,
    private val viewModel: HistoryViewModel
) {
    private val taskWorkManager = TaskWorkManager.getInstance(context)
    private val taskRepository = TaskRepository.getInstance(context)

    fun cancelTask(task: Task) {
        AlertDialog.Builder(context)
            .setTitle("작업 취소")
            .setMessage("이 작업을 취소하시겠습니까?")
            .setPositiveButton("취소") { _, _ ->
                performCancelTask(task)
            }
            .setNegativeButton("아니오", null)
            .show()
    }

    fun retryTask(task: Task) {
        AlertDialog.Builder(context)
            .setTitle("작업 재시도")
            .setMessage("이 작업을 다시 시도하시겠습니까?")
            .setPositiveButton("재시도") { _, _ ->
                performRetryTask(task)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    fun deleteSelectedTasks(tasks: List<Task>, onComplete: () -> Unit) {
        val activeCount = tasks.count { it.status in listOf(TaskStatus.RUNNING, TaskStatus.PENDING) }
        val message = if (activeCount > 0) {
            "${tasks.size}개의 작업을 삭제하시겠습니까?(진행/대기 중인 작업 ${activeCount}개가 중단됩니다)"
        } else {
            "${tasks.size}개의 작업을 삭제하시겠습니까?"
        }

        AlertDialog.Builder(context)
            .setTitle("작업 삭제")
            .setMessage(message)
            .setPositiveButton("삭제") { _, _ ->
                performDeleteTasks(tasks, onComplete)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun performCancelTask(task: Task) {
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

    private fun performRetryTask(task: Task) {
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

    private fun performDeleteTasks(tasks: List<Task>, onComplete: () -> Unit) {
        val progressDialog = ProgressDialog(context).apply {
            setMessage("삭제 중...")
            setCancelable(false)
            show()
        }

        (context as? HistoryActivity)?.lifecycleScope?.launch {
            try {
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

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    onComplete()
                    showToast("${tasks.size}개의 작업이 삭제되었습니다")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    showToast("삭제 중 오류가 발생했습니다")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}