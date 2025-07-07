// model/presentation/adapter/task/TaskDiffCallback.kt
package com.ssc.namespring.model.presentation.adapter.task

import androidx.recyclerview.widget.DiffUtil
import com.ssc.namespring.model.domain.entity.Task

class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem &&
                oldItem.status == newItem.status &&
                oldItem.progress == newItem.progress
    }
}