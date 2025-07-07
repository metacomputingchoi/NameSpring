// model/presentation/adapter/TaskHistoryAdapter.kt
package com.ssc.namespring.model.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namespring.model.presentation.adapter.task.*

class TaskHistoryAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskLongClick: (Task) -> Boolean,
    private val onTaskCancel: (Task) -> Unit,
    private val onTaskRetry: (Task) -> Unit,
    private val selectionListener: SelectionListener? = null
) : ListAdapter<Task, TaskViewHolder>(TaskDiffCallback()),
    TaskViewHolder.ClickHandlers {

    private val selectionManager = TaskSelectionManager()

    interface SelectionListener {
        fun onSelectionChanged(selectedCount: Int)
    }

    var isSelectionMode: Boolean
        get() = selectionManager.isSelectionMode
        set(value) {
            if (value) {
                selectionManager.enableSelectionMode()
            } else {
                selectionManager.disableSelectionMode()
            }
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_history, parent, false)
        return TaskViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        val isSelected = selectionManager.isSelected(task.id)
        val queuePosition = getQueuePosition(task)
        holder.bind(task, isSelectionMode, isSelected, queuePosition)
    }

    override fun onItemClick(task: Task) {
        onTaskClick(task)
    }

    override fun onItemLongClick(task: Task): Boolean {
        return onTaskLongClick(task)
    }

    override fun onActionClick(task: Task) {
        when (task.status) {
            TaskStatus.RUNNING, TaskStatus.PENDING -> onTaskCancel(task)
            TaskStatus.CANCELLED, TaskStatus.FAILED -> onTaskRetry(task)
            else -> {}
        }
    }

    override fun onSelectionToggle(taskId: String) {
        toggleSelection(taskId)
    }

    fun toggleSelection(taskId: String) {
        selectionManager.toggleSelection(taskId)
        notifyItemChanged(currentList.indexOfFirst { it.id == taskId })
        selectionListener?.onSelectionChanged(selectionManager.getSelectedCount())
    }

    fun selectVisible() {
        selectionManager.selectAll(currentList)
        notifyDataSetChanged()
        selectionListener?.onSelectionChanged(selectionManager.getSelectedCount())
    }

    fun deselectVisible() {
        selectionManager.deselectAll(currentList)
        notifyDataSetChanged()
        selectionListener?.onSelectionChanged(selectionManager.getSelectedCount())
    }

    fun clearSelection() {
        selectionManager.clearSelection()
        notifyDataSetChanged()
        selectionListener?.onSelectionChanged(0)
    }

    fun getSelectedTaskIds(): Set<String> = selectionManager.getSelectedIds()
    fun getSelectedCount(): Int = selectionManager.getSelectedCount()
    fun getVisibleSelectedCount(): Int = selectionManager.getVisibleSelectedCount(currentList)

    private fun getQueuePosition(task: Task): Int {
        val pendingTasks = currentList.filter { it.status == TaskStatus.PENDING }
            .sortedBy { it.createdAt }
        return pendingTasks.indexOf(task) + 1
    }
}