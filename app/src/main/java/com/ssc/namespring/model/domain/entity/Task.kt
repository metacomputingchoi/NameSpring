// model/domain/entity/Task.kt
package com.ssc.namespring.model.domain.entity

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val type: TaskType,
    val status: TaskStatus = TaskStatus.PENDING,
    val inputData: Map<String, Any> = emptyMap(),
    val outputData: Map<String, Any>? = null,
    val progress: Int = 0,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val workerId: String? = null
) {
    fun isActive(): Boolean = status in listOf(TaskStatus.RUNNING, TaskStatus.PENDING)

    fun getDuration(): Long? {
        return if (startedAt != null && completedAt != null) {
            completedAt - startedAt
        } else null
    }
}

enum class TaskType {
    NAMING,           // 작명
    EVALUATION,       // 평가
    COMPARISON,       // 비교
    REPORT_GENERATION // 보고서 생성
}

enum class TaskStatus {
    PENDING,    // 대기중
    RUNNING,    // 진행중
    COMPLETED,  // 완료
    FAILED,     // 실패
    CANCELLED   // 취소됨
}