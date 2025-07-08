// ui/history/HistoryTaskActionHandler.kt
package com.ssc.namespring.ui.history

import android.content.Context
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.service.workmanager.TaskWorkManager
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.ui.history.handlers.*

/**
 * Task 관련 액션을 처리하는 Facade 클래스
 * 기존 인터페이스를 유지하면서 내부적으로 책임을 분리
 */
class HistoryTaskActionHandler(
    private val context: Context,
    private val viewModel: HistoryViewModel
) : ITaskActionHandler {

    private val taskWorkManager = TaskWorkManager.getInstance(context)
    private val taskRepository = TaskRepository.getInstance(context)

    private val dialogHandler = TaskDialogHandler(context)
    private val taskExecutor = TaskExecutor(
        context, 
        taskWorkManager, 
        taskRepository
    )

    override fun cancelTask(task: Task) {
        dialogHandler.showCancelDialog(task) {
            taskExecutor.cancelTask(task)
        }
    }

    override fun retryTask(task: Task) {
        dialogHandler.showRetryDialog(task) {
            taskExecutor.retryTask(task)
        }
    }

    override fun deleteSelectedTasks(tasks: List<Task>, onComplete: () -> Unit) {
        dialogHandler.showDeleteDialog(tasks) {
            taskExecutor.deleteTasks(tasks, onComplete)
        }
    }
}