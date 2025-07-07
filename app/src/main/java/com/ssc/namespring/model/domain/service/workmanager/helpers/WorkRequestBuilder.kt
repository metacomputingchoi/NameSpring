// model/domain/service/workmanager/helpers/WorkRequestBuilder.kt
package com.ssc.namespring.model.domain.service.workmanager.helpers

import androidx.work.*
import com.google.gson.Gson
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.model.domain.service.workmanager.workers.*

internal class WorkRequestBuilder {
    private val gson = Gson()

    fun buildWorkRequest(task: Task): OneTimeWorkRequest {
        val inputData = createInputData(task)
        val constraints = createConstraints()

        return when (task.type) {
            TaskType.NAMING -> OneTimeWorkRequestBuilder<NamingWorker>()
            TaskType.EVALUATION -> OneTimeWorkRequestBuilder<EvaluationWorker>()
            TaskType.COMPARISON -> OneTimeWorkRequestBuilder<ComparisonWorker>()
            TaskType.REPORT_GENERATION -> OneTimeWorkRequestBuilder<ReportGenerationWorker>()
        }
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag("profile_${task.profileId}")
            .addTag("type_${task.type.name}")
            .addTag("task_${task.id}")
            .build()
    }

    private fun createInputData(task: Task): Data {
        return Data.Builder()
            .putString("task_id", task.id)
            .putString("profile_id", task.profileId)
            .putString("task_type", task.type.name)
            .putString("input_data", gson.toJson(task.inputData))
            .build()
    }

    private fun createConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
    }
}