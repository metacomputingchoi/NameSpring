// model/domain/service/workmanager/interfaces/ITaskWorkManager.kt
package com.ssc.namespring.model.domain.service.workmanager.interfaces

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskHistory

interface ITaskWorkManager {
    val activeTasks: LiveData<List<Task>>
    val taskHistory: LiveData<Map<String, TaskHistory>>

    fun enqueueTask(task: Task): String
    fun cancelTask(taskId: String)
    fun cancelTasksByProfile(profileId: String)
    fun getTaskProgress(taskId: String): LiveData<WorkInfo?>
}