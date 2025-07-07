// ui/history/components/taskdetail/TaskDetailNameResultBuilder.kt
package com.ssc.namespring.ui.history.components.taskdetail

import android.annotation.SuppressLint
import android.content.Context
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namingengine.data.GeneratedName

class TaskDetailNameResultBuilder(
    private val context: Context,
    private val gson: Gson
) {
    private val componentBuilder = TaskDetailComponentBuilder(context)

    fun processTaskResult(container: LinearLayout, task: Task, rawData: String) {
        if (task.type == TaskType.NAMING) {
            try {
                val type = object : TypeToken<List<GeneratedName>>() {}.type
                val generatedNames: List<GeneratedName> = gson.fromJson(rawData, type)

                container.apply {
                    addView(componentBuilder.createInfoRow("생성된 이름 수", "${generatedNames.size}개"))

                    val namesToShow = generatedNames.take(10)
                    addView(componentBuilder.createSectionTitle("상위 ${namesToShow.size}개 이름"))

                    namesToShow.forEachIndexed { index, name ->
                        addView(createNameView(index + 1, name))
                    }

                    if (generatedNames.size > 10) {
                        addView(componentBuilder.createInfoRow("", "... 외 ${generatedNames.size - 10}개"))
                    }
                }
            } catch (e: Exception) {
                container.addView(createTruncatedCodeView(rawData))
            }
        } else {
            container.addView(createTruncatedCodeView(rawData))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createNameView(index: Int, name: GeneratedName): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 16)

            addView(TextView(context).apply {
                text = "$index. ${name.combinedPronounciation} (${name.combinedHanja})"
                textSize = 16f
                setTextColor(context.getColor(android.R.color.black))
            })

            name.analysisInfo?.let { info ->
                addView(TextView(context).apply {
                    text = "총점: ${info.totalScore}점"
                    textSize = 14f
                    setTextColor(context.getColor(android.R.color.darker_gray))
                })
            }

            val meaning = name.hanjaDetails
                .drop(1)
                .mapNotNull { it.inmyongMeaning.takeIf { meaning -> meaning.isNotBlank() } }
                .joinToString(", ")

            if (meaning.isNotEmpty()) {
                addView(TextView(context).apply {
                    text = "의미: $meaning"
                    textSize = 14f
                    setTextColor(context.getColor(android.R.color.darker_gray))
                })
            }
        }
    }

    private fun createTruncatedCodeView(rawData: String): HorizontalScrollView {
        val truncated = rawData.take(1000) + if (rawData.length > 1000) "... (전체 ${rawData.length}자)" else ""
        return componentBuilder.createCodeView(truncated)
    }
}