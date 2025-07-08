// model/domain/service/workmanager/WorkResult.kt
package com.ssc.namespring.model.domain.service.workmanager

/**
 * Worker 작업 결과를 나타내는 데이터 클래스
 */
data class WorkResult(
    val success: Boolean,
    val data: Map<String, Any>? = null,
    val rawData: String? = null,  // 대용량 데이터용
    val error: String? = null
)