// ui/history/TaskDetailDialog.kt
package com.ssc.namespring.ui.history

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.ui.history.components.taskdetail.*
import com.ssc.namespring.ui.history.handlers.TaskDetailDataHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class TaskDetailDialog(
    context: Context,
    private val task: Task,
    private val taskRepository: TaskRepository
) : Dialog(context, android.R.style.Theme_Material_Light_Dialog_Alert) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var viewBuilder: TaskDetailViewBuilder
    private lateinit var dataHandler: TaskDetailDataHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDialog()
        initializeComponents()
        setupViews()
    }

    private fun setupDialog() {
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initializeComponents() {
        viewBuilder = TaskDetailViewBuilder(context)
        dataHandler = TaskDetailDataHandler(
            context = context,
            task = task,
            taskRepository = taskRepository,
            scope = scope,
            viewBuilder = viewBuilder
        )
    }

    private fun setupViews() {
        val scrollView = NestedScrollView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val contentView = viewBuilder.buildCompleteView(
            task = task,
            dataHandler = dataHandler,
            onDismiss = { dismiss() }
        )

        scrollView.addView(contentView)
        setContentView(scrollView)
    }
}