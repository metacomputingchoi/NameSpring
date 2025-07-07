// ui/history/handlers/TaskDetailDataHandler.kt
package com.ssc.namespring.ui.history.handlers

import android.content.Context
import android.widget.LinearLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskResult
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.ui.history.components.taskdetail.TaskDetailComponentBuilder
import com.ssc.namespring.ui.history.components.taskdetail.TaskDetailNameResultBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TaskDetailDataHandler(
    private val context: Context,
    private val task: Task,
    private val taskRepository: TaskRepository,
    private val scope: CoroutineScope,
    private val viewBuilder: Any
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val componentBuilder = TaskDetailComponentBuilder(context)
    private val nameResultBuilder = TaskDetailNameResultBuilder(context, gson)
    private val jsonHandler = TaskDetailJsonHandler(context, gson)

    fun formatInputData(task: Task): String {
        return gson.toJson(task.inputData)
    }

    fun handleCompletedTask(container: LinearLayout) {
        val result = taskRepository.getTaskResult(task.id)
        result?.let {
            container.apply {
                addView(componentBuilder.createDivider())
                addView(componentBuilder.createSectionTitle("결과 데이터"))

                it.data?.let { data ->
                    addView(componentBuilder.createCodeView(gson.toJson(data)))
                }

                handleRawData(this, it)
            }
        }
    }

    private fun handleRawData(container: LinearLayout, result: TaskResult) {
        if (result.rawData != null) {
            processRawData(container, result.rawData)
        } else if (result.data?.containsKey("rawDataFile") == true) {
            val filePath = result.data["rawDataFile"] as? String
            if (filePath != null) {
                scope.launch {
                    val rawData = taskRepository.loadRawDataFromFile(filePath)
                    rawData?.let {
                        processRawData(container, it)
                    }
                }
            }
        }
    }

    private fun processRawData(container: LinearLayout, rawData: String) {
        container.apply {
            addView(componentBuilder.createDivider())
            addView(componentBuilder.createSectionTitle("전체 결과 데이터"))

            nameResultBuilder.processTaskResult(this, task, rawData)
            addView(jsonHandler.createRawDataButtons(rawData))
        }
    }
}