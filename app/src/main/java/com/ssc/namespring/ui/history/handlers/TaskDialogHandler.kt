// ui/history/handlers/TaskDialogHandler.kt
package com.ssc.namespring.ui.history.handlers

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskStatus

class TaskDialogHandler(private val context: Context) {

    fun showCancelDialog(task: Task, onConfirm: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("작업 취소")
            .setMessage("이 작업을 취소하시겠습니까?")
            .setPositiveButton("취소") { _, _ -> onConfirm() }
            .setNegativeButton("아니오", null)
            .show()
    }

    fun showRetryDialog(task: Task, onConfirm: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("작업 재시도")
            .setMessage("이 작업을 다시 시도하시겠습니까?")
            .setPositiveButton("재시도") { _, _ -> onConfirm() }
            .setNegativeButton("취소", null)
            .show()
    }

    fun showDeleteDialog(tasks: List<Task>, onConfirm: () -> Unit) {
        val activeCount = tasks.count { 
            it.status in listOf(TaskStatus.RUNNING, TaskStatus.PENDING) 
        }
        val message = if (activeCount > 0) {
            "${'$'}{tasks.size}개의 작업을 삭제하시겠습니까?" +
            "(진행/대기 중인 작업 ${'$'}{activeCount}개가 중단됩니다)"
        } else {
            "${'$'}{tasks.size}개의 작업을 삭제하시겠습니까?"
        }

        AlertDialog.Builder(context)
            .setTitle("작업 삭제")
            .setMessage(message)
            .setPositiveButton("삭제") { _, _ -> onConfirm() }
            .setNegativeButton("취소", null)
            .show()
    }
}