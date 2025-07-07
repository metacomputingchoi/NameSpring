package com.ssc.namespring.model.presentation.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
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
    private val onTaskLongClick: (Task) -> Boolean,
    private val onTaskCancel: (Task) -> Unit,  // 추가
    private val onTaskRetry: (Task) -> Unit,   // 추가
    private val selectionListener: SelectionListener? = null
) : ListAdapter<Task, TaskHistoryAdapter.TaskViewHolder>(TaskDiffCallback()) {

    var isSelectionMode = false
        set(value) {
            field = value
            if (!value) {
                selectedTaskIds.clear()
            }
            notifyDataSetChanged()
        }

    private val selectedTaskIds = mutableSetOf<String>()
    private val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    interface SelectionListener {
        fun onSelectionChanged(selectedCount: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_history, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun toggleSelection(taskId: String) {
        if (selectedTaskIds.contains(taskId)) {
            selectedTaskIds.remove(taskId)
        } else {
            selectedTaskIds.add(taskId)
        }

        currentList.forEachIndexed { index, task ->
            if (task.id == taskId) {
                notifyItemChanged(index)
                return@forEachIndexed
            }
        }

        selectionListener?.onSelectionChanged(selectedTaskIds.size)
    }

    fun selectVisible() {
        currentList.forEach { task ->
            selectedTaskIds.add(task.id)
        }
        notifyDataSetChanged()
        selectionListener?.onSelectionChanged(selectedTaskIds.size)
    }

    fun deselectVisible() {
        currentList.forEach { task ->
            selectedTaskIds.remove(task.id)
        }
        notifyDataSetChanged()
        selectionListener?.onSelectionChanged(selectedTaskIds.size)
    }

    fun clearSelection() {
        selectedTaskIds.clear()
        notifyDataSetChanged()
        selectionListener?.onSelectionChanged(0)
    }

    fun getSelectedTaskIds(): Set<String> = selectedTaskIds.toSet()
    fun getSelectedCount(): Int = selectedTaskIds.size
    fun getVisibleSelectedCount(): Int = currentList.count { it.id in selectedTaskIds }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        private val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
        private val tvTaskType: TextView = itemView.findViewById(R.id.tvTaskType)
        private val tvTaskStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        private val tvTaskTime: TextView = itemView.findViewById(R.id.tvTaskTime)
        private val tvTaskDuration: TextView = itemView.findViewById(R.id.tvTaskDuration)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val btnAction: ImageButton = itemView.findViewById(R.id.btnAction)

        private fun getQueuePosition(task: Task): Int {
            val pendingTasks = currentList.filter { it.status == TaskStatus.PENDING }
                .sortedBy { it.createdAt }
            return pendingTasks.indexOf(task) + 1
        }

        fun bind(task: Task) {
            // 선택 모드 UI
            if (isSelectionMode) {
                checkBox.visibility = View.VISIBLE
                btnAction.visibility = View.GONE
            } else {
                checkBox.visibility = View.GONE
                // 액션 버튼 표시 여부
                when (task.status) {
                    TaskStatus.RUNNING, TaskStatus.PENDING -> {
                        btnAction.visibility = View.VISIBLE
                        btnAction.setImageResource(R.drawable.ic_cancel)
                        btnAction.contentDescription = "작업 취소"
                    }
                    TaskStatus.CANCELLED, TaskStatus.FAILED -> {
                        btnAction.visibility = View.VISIBLE
                        btnAction.setImageResource(R.drawable.ic_replay)
                        btnAction.contentDescription = "다시 시도"
                    }
                    else -> {
                        btnAction.visibility = View.GONE
                    }
                }
            }

            checkBox.isChecked = selectedTaskIds.contains(task.id)
            checkBox.setOnClickListener {
                toggleSelection(task.id)
            }

            // 선택된 아이템 하이라이트
            cardView.setCardBackgroundColor(
                if (selectedTaskIds.contains(task.id))
                    Color.parseColor("#E3F2FD")
                else
                    Color.WHITE
            )

            // 작업명 표시
            tvTaskName.text = task.inputData["profileName"] as? String ?: "작업 ${task.id.take(8)}"

            // 작업 유형
            tvTaskType.text = when (task.type) {
                TaskType.NAMING -> "작명"
                TaskType.EVALUATION -> "평가"
                TaskType.COMPARISON -> "비교"
                TaskType.REPORT_GENERATION -> "보고서"
            }

            // 상태 표시
            tvTaskStatus.text = when (task.status) {
                TaskStatus.PENDING -> "대기중 #${getQueuePosition(task)}"  // 큐 위치 표시
                TaskStatus.RUNNING -> "진행중"
                TaskStatus.COMPLETED -> "완료"
                TaskStatus.FAILED -> "실패"
                TaskStatus.CANCELLED -> "취소됨"
            }

            // 상태별 색상
            tvTaskStatus.setTextColor(
                when (task.status) {
                    TaskStatus.RUNNING -> Color.parseColor("#2196F3")
                    TaskStatus.COMPLETED -> Color.parseColor("#4CAF50")
                    TaskStatus.FAILED -> Color.parseColor("#F44336")
                    TaskStatus.CANCELLED -> Color.parseColor("#FF9800")
                    else -> Color.parseColor("#FFC107")
                }
            )

            // 시간 정보
            tvTaskTime.text = dateFormat.format(Date(task.createdAt))

            // 소요 시간
            tvTaskDuration.text = if (task.completedAt != null && task.startedAt != null) {
                val duration = (task.completedAt - task.startedAt) / 1000
                when {
                    duration < 60 -> "${duration}초"
                    duration < 3600 -> "${duration / 60}분"
                    else -> "${duration / 3600}시간"
                }
            } else {
                ""
            }

            // 진행률
            if (task.status == TaskStatus.RUNNING) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = task.progress
            } else {
                progressBar.visibility = View.GONE
            }

            // 액션 버튼 클릭
            btnAction.setOnClickListener {
                when (task.status) {
                    TaskStatus.RUNNING, TaskStatus.PENDING -> onTaskCancel(task)
                    TaskStatus.CANCELLED, TaskStatus.FAILED -> onTaskRetry(task)
                    else -> {}
                }
            }

            // 아이템 클릭 이벤트
            itemView.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(task.id)
                } else {
                    onTaskClick(task)
                }
            }

            itemView.setOnLongClickListener { onTaskLongClick(task) }
        }
    }

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
}