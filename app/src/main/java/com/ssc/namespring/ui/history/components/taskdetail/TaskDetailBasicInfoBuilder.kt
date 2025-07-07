// ui/history/components/taskdetail/TaskDetailBasicInfoBuilder.kt
package com.ssc.namespring.ui.history.components.taskdetail

import android.content.Context
import android.widget.LinearLayout
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.ui.history.formatters.TaskDetailFormatter

class TaskDetailBasicInfoBuilder(
    private val context: Context
) {
    private val formatter = TaskDetailFormatter()
    private val componentBuilder = TaskDetailComponentBuilder(context)

    fun addBasicInfo(container: LinearLayout, task: Task) {
        container.apply {
            addView(componentBuilder.createSectionTitle("기본 정보"))
            addView(componentBuilder.createInfoRow("작업 ID", task.id))
            addView(componentBuilder.createInfoRow("작업 유형", formatter.getTaskTypeName(task.type)))
            addView(componentBuilder.createInfoRow("상태", task.status.name))
            addView(componentBuilder.createInfoRow("생성 시간", formatter.formatDateTime(task.createdAt)))

            task.startedAt?.let {
                addView(componentBuilder.createInfoRow("시작 시간", formatter.formatDateTime(it)))
            }

            task.completedAt?.let {
                addView(componentBuilder.createInfoRow("완료 시간", formatter.formatDateTime(it)))
            }

            task.getDuration()?.let {
                addView(componentBuilder.createInfoRow("소요 시간", formatter.formatDuration(it)))
            }
        }
    }
}