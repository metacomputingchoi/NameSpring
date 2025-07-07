// model/presentation/adapter/TaskHistoryAdapter.kt
package com.ssc.namespring.model.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namespring.model.domain.entity.TaskType
import java.text.SimpleDateFormat
import java.util.*

class TaskHistoryAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskCancel: (Task) -> Unit,
    private val onTaskRetry: (Task) -> Unit
) : ListAdapter<Task, TaskHistoryAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_history, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTaskType: TextView = itemView.findViewById(R.id.tvTaskType)
        private val tvTaskStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        private val tvTaskTime: TextView = itemView.findViewById(R.id.tvTaskTime)
        private val tvTaskDuration: TextView = itemView.findViewById(R.id.tvTaskDuration)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val btnAction: ImageButton = itemView.findViewById(R.id.btnAction)

        fun bind(task: Task) {
            tvTaskType.text = getTaskTypeName(task.type)
            tvTaskStatus.text = getStatusText(task.status)
            tvTaskTime.text = formatTime(task.createdAt)

            task.getDuration()?.let { duration ->
                tvTaskDuration.text = formatDuration(duration)
                tvTaskDuration.visibility = View.VISIBLE
            } ?: run {
                tvTaskDuration.visibility = View.GONE
            }

            // Set status color
            val statusColor = when (task.status) {
                TaskStatus.COMPLETED -> R.color.success_green
                TaskStatus.FAILED -> R.color.error_red
                TaskStatus.RUNNING -> R.color.primary_blue
                TaskStatus.CANCELLED -> R.color.text_secondary
                else -> R.color.text_primary
            }
            tvTaskStatus.setTextColor(itemView.context.getColor(statusColor))

            // Handle progress bar
            if (task.status == TaskStatus.RUNNING) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = task.progress
            } else {
                progressBar.visibility = View.GONE
            }

            // Handle action button
            when (task.status) {
                TaskStatus.RUNNING -> {
                    btnAction.visibility = View.VISIBLE
                    btnAction.setImageResource(R.drawable.ic_cancel)
                    btnAction.setOnClickListener { onTaskCancel(task) }
                }
                TaskStatus.FAILED, TaskStatus.CANCELLED -> {
                    btnAction.visibility = View.VISIBLE
                    btnAction.setImageResource(R.drawable.ic_retry)
                    btnAction.setOnClickListener { onTaskRetry(task) }
                }
                else -> {
                    btnAction.visibility = View.GONE
                }
            }

            itemView.setOnClickListener { onTaskClick(task) }
        }

        private fun getTaskTypeName(type: TaskType): String {
            return when (type) {
                TaskType.NAMING -> "작명"
                TaskType.EVALUATION -> "평가"
                TaskType.COMPARISON -> "비교"
                TaskType.REPORT_GENERATION -> "보고서 생성"
            }
        }

        private fun getStatusText(status: TaskStatus): String {
            return when (status) {
                TaskStatus.PENDING -> "대기중"
                TaskStatus.RUNNING -> "진행중"
                TaskStatus.COMPLETED -> "완료"
                TaskStatus.FAILED -> "실패"
                TaskStatus.CANCELLED -> "취소됨"
            }
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        private fun formatDuration(duration: Long): String {
            val seconds = duration / 1000
            return when {
                seconds < 60 -> "${seconds}초"
                seconds < 3600 -> "${seconds / 60}분 ${seconds % 60}초"
                else -> "${seconds / 3600}시간 ${(seconds % 3600) / 60}분"
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}