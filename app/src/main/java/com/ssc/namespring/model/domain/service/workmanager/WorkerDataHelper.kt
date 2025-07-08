// model/domain/service/workmanager/WorkerDataHelper.kt
package com.ssc.namespring.model.domain.service.workmanager

import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.domain.entity.TaskType

/**
 * Worker 입력 데이터 처리 헬퍼
 */
class WorkerDataHelper(private val inputData: Data) {
    private val gson = Gson()

    val taskId: String
        get() = inputData.getString("task_id") ?: ""

    val profileId: String
        get() = inputData.getString("profile_id") ?: ""

    val taskType: TaskType
        get() = TaskType.valueOf(inputData.getString("task_type") ?: TaskType.EVALUATION.name)

    fun getInputDataMap(): Map<String, Any> {
        val jsonString = inputData.getString("input_data") ?: "{}"
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(jsonString, type)
    }
}