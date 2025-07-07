// ui/history/components/taskdetail/TaskDetailResultViewBuilder.kt
package com.ssc.namespring.ui.history.components.taskdetail

import android.content.Context
import android.widget.LinearLayout
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namespring.ui.history.handlers.TaskDetailDataHandler

class TaskDetailResultViewBuilder(
    private val context: Context
) {
    private val componentBuilder = TaskDetailComponentBuilder(context)

    fun addResultViews(
        container: LinearLayout,
        task: Task,
        dataHandler: TaskDetailDataHandler
    ) {
        when (task.status) {
            TaskStatus.COMPLETED -> {
                dataHandler.handleCompletedTask(container)
            }
            TaskStatus.FAILED -> {
                task.errorMessage?.let {
                    container.apply {
                        addView(componentBuilder.createDivider())
                        addView(componentBuilder.createSectionTitle("에러 메시지"))
                        addView(componentBuilder.createErrorView(it))
                    }
                }
            }
            else -> {}
        }
    }
}