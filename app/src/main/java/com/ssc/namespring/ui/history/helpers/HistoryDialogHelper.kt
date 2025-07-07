// ui/history/helpers/HistoryDialogHelper.kt
package com.ssc.namespring.ui.history.helpers

import androidx.fragment.app.FragmentManager
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.ui.history.NameListDialogFragment
import com.ssc.namespring.ui.history.TaskDetailDialog
import com.ssc.namespring.model.data.repository.TaskRepository

object HistoryDialogHelper {
    fun showTaskDetail(fragmentManager: FragmentManager, task: Task) {
        when (task.type) {
            TaskType.NAMING, TaskType.EVALUATION -> {
                NameListDialogFragment.newInstance(task.id)
                    .show(fragmentManager, "NameListDialog")
            }
            else -> {
                val context = fragmentManager.fragments.firstOrNull()?.context
                context?.let {
                    TaskDetailDialog(it, task, TaskRepository.getInstance(it)).show()
                }
            }
        }
    }
}