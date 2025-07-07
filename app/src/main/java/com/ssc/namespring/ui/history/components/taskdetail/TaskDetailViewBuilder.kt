// ui/history/components/taskdetail/TaskDetailViewBuilder.kt
package com.ssc.namespring.ui.history.components.taskdetail

import android.content.Context
import android.widget.LinearLayout
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.ui.history.handlers.TaskDetailDataHandler

class TaskDetailViewBuilder(
    private val context: Context
) {
    private val basicInfoBuilder = TaskDetailBasicInfoBuilder(context)
    private val resultViewBuilder = TaskDetailResultViewBuilder(context)
    private val componentBuilder = TaskDetailComponentBuilder(context)

    fun buildCompleteView(
        task: Task,
        dataHandler: TaskDetailDataHandler,
        onDismiss: () -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)

            // 제목
            addView(componentBuilder.createTitleView("작업 상세 정보"))
            addView(componentBuilder.createDivider())

            // 기본 정보
            basicInfoBuilder.addBasicInfo(this, task)
            addView(componentBuilder.createDivider())

            // 입력 데이터
            addView(componentBuilder.createSectionTitle("입력 데이터"))
            addView(componentBuilder.createCodeView(dataHandler.formatInputData(task)))

            // 결과 데이터
            resultViewBuilder.addResultViews(this, task, dataHandler)

            addView(componentBuilder.createDivider())

            // 버튼
            addView(componentBuilder.createButtonsLayout(onDismiss))
        }
    }
}