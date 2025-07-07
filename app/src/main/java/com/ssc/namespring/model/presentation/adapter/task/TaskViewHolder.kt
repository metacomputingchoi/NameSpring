// model/presentation/adapter/task/TaskViewHolder.kt
package com.ssc.namespring.model.presentation.adapter.task

import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskStatus

class TaskViewHolder(
    itemView: View,
    private val clickHandlers: ClickHandlers
) : RecyclerView.ViewHolder(itemView) {

    interface ClickHandlers {
        fun onItemClick(task: Task)
        fun onItemLongClick(task: Task): Boolean
        fun onActionClick(task: Task)
        fun onSelectionToggle(taskId: String)
    }

    private val cardView: CardView = itemView.findViewById(R.id.cardView)
    private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    private val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
    private val tvTaskType: TextView = itemView.findViewById(R.id.tvTaskType)
    private val tvTaskStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
    private val tvTaskTime: TextView = itemView.findViewById(R.id.tvTaskTime)
    private val tvTaskDuration: TextView = itemView.findViewById(R.id.tvTaskDuration)
    private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    private val btnAction: ImageButton = itemView.findViewById(R.id.btnAction)

    fun bind(task: Task, isSelectionMode: Boolean, isSelected: Boolean, queuePosition: Int) {
        updateSelectionUI(isSelectionMode, isSelected, task)
        updateTaskInfo(task, queuePosition)
        updateActionButton(task, isSelectionMode)
        updateProgress(task)
        setClickListeners(task, isSelectionMode)
    }

    private fun updateSelectionUI(isSelectionMode: Boolean, isSelected: Boolean, task: Task) {
        checkBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        checkBox.isChecked = isSelected
        cardView.setCardBackgroundColor(TaskUIHelper.getSelectionBackgroundColor(isSelected))
    }

    private fun updateTaskInfo(task: Task, queuePosition: Int) {
        tvTaskName.text = TaskUIHelper.getTaskName(task)
        tvTaskType.text = TaskUIHelper.getTaskTypeText(task.type)
        tvTaskStatus.text = TaskUIHelper.getTaskStatusText(task, queuePosition)
        tvTaskStatus.setTextColor(TaskUIHelper.getStatusColor(task.status))
        tvTaskTime.text = TaskUIHelper.formatTaskTime(task.createdAt)
        tvTaskDuration.text = TaskUIHelper.formatDuration(task)
    }

    private fun updateActionButton(task: Task, isSelectionMode: Boolean) {
        if (isSelectionMode) {
            btnAction.visibility = View.GONE
        } else {
            val buttonInfo = TaskUIHelper.getActionButtonResource(task.status)
            if (buttonInfo != null) {
                btnAction.visibility = View.VISIBLE
                btnAction.setImageResource(buttonInfo.first)
                btnAction.contentDescription = buttonInfo.second
            } else {
                btnAction.visibility = View.GONE
            }
        }
    }

    private fun updateProgress(task: Task) {
        progressBar.visibility = if (task.status == TaskStatus.RUNNING) View.VISIBLE else View.GONE
        progressBar.progress = task.progress
    }

    private fun setClickListeners(task: Task, isSelectionMode: Boolean) {
        checkBox.setOnClickListener { clickHandlers.onSelectionToggle(task.id) }
        btnAction.setOnClickListener { clickHandlers.onActionClick(task) }

        itemView.setOnClickListener {
            if (isSelectionMode) {
                clickHandlers.onSelectionToggle(task.id)
            } else {
                clickHandlers.onItemClick(task)
            }
        }

        itemView.setOnLongClickListener { clickHandlers.onItemLongClick(task) }
    }
}