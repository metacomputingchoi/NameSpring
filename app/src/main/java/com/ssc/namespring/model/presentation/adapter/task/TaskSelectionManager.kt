// model/presentation/adapter/task/TaskSelectionManager.kt
package com.ssc.namespring.model.presentation.adapter.task

import com.ssc.namespring.model.domain.entity.Task

class TaskSelectionManager {
    private val selectedTaskIds = mutableSetOf<String>()
    var isSelectionMode = false
        private set

    interface SelectionListener {
        fun onSelectionChanged(selectedCount: Int)
    }

    fun enableSelectionMode() {
        isSelectionMode = true
    }

    fun disableSelectionMode() {
        isSelectionMode = false
        selectedTaskIds.clear()
    }

    fun toggleSelection(taskId: String): Boolean {
        return if (selectedTaskIds.contains(taskId)) {
            selectedTaskIds.remove(taskId)
            false
        } else {
            selectedTaskIds.add(taskId)
            true
        }
    }

    fun selectAll(tasks: List<Task>) {
        tasks.forEach { selectedTaskIds.add(it.id) }
    }

    fun deselectAll(tasks: List<Task>) {
        tasks.forEach { selectedTaskIds.remove(it.id) }
    }

    fun clearSelection() {
        selectedTaskIds.clear()
    }

    fun isSelected(taskId: String): Boolean = selectedTaskIds.contains(taskId)

    fun getSelectedIds(): Set<String> = selectedTaskIds.toSet()

    fun getSelectedCount(): Int = selectedTaskIds.size

    fun getVisibleSelectedCount(tasks: List<Task>): Int = 
        tasks.count { it.id in selectedTaskIds }
}