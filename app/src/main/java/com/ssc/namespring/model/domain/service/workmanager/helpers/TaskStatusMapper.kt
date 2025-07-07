// model/domain/service/workmanager/helpers/TaskStatusMapper.kt
package com.ssc.namespring.model.domain.service.workmanager.helpers

import androidx.work.WorkInfo
import com.ssc.namespring.model.domain.entity.TaskStatus

internal class TaskStatusMapper {
    fun mapWorkInfoToTaskStatus(workInfoState: WorkInfo.State): TaskStatus? {
        return when (workInfoState) {
            WorkInfo.State.RUNNING -> TaskStatus.RUNNING
            WorkInfo.State.SUCCEEDED -> TaskStatus.COMPLETED
            WorkInfo.State.FAILED -> TaskStatus.FAILED
            WorkInfo.State.CANCELLED -> TaskStatus.CANCELLED
            else -> null // ENQUEUED, BLOCKED - 대기 상태
        }
    }

    fun extractTaskIdFromTags(tags: Set<String>): String? {
        return tags.find { it.startsWith("task_") }
            ?.removePrefix("task_")
    }
}