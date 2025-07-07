// model/domain/entity/TaskResult.kt
package com.ssc.namespring.model.domain.entity

import com.google.gson.annotations.SerializedName

data class TaskResult(
    @SerializedName("task_id")
    val taskId: String,

    @SerializedName("task_type")
    val taskType: TaskType,

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: Map<String, Any>? = null,

    @SerializedName("raw_data")
    val rawData: String? = null,  // GeneratedName 리스트를 JSON으로 저장

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

data class TaskHistory(
    @SerializedName("profile_id")
    val profileId: String,

    @SerializedName("tasks")
    val tasks: List<Task> = emptyList()
) {
    fun getActiveTasks(): List<Task> = tasks.filter { it.isActive() }
    fun getCompletedTasks(): List<Task> = tasks.filter { it.status == TaskStatus.COMPLETED }
    fun getTasksByType(type: TaskType): List<Task> = tasks.filter { it.type == type }
}