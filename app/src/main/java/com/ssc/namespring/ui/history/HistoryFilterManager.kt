// ui/history/HistoryFilterManager.kt
package com.ssc.namespring.ui.history

import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namespring.utils.search.TaskSearchHelper

class HistoryFilterManager {
    enum class TaskFilter {
        ALL, COMPLETED, ACTIVE_QUEUE, CANCELLED, FAILED
    }

    enum class SortOrder {
        DATE_DESC, DATE_ASC, NAME_ASC, NAME_DESC, TYPE, STATUS
    }

    var currentFilter: TaskFilter = TaskFilter.ALL
    var currentSortOrder: SortOrder = SortOrder.DATE_DESC
    var currentSearchQuery: String = ""
    var selectedTypes: Set<String> = emptySet()

    private val searchHelper = TaskSearchHelper()

    fun applyFiltersAndSort(tasks: List<Task>): List<Task> {
        return tasks
            .filter { task -> matchesAllFilters(task) }
            .let { filteredTasks -> sortTasks(filteredTasks) }
    }

    private fun matchesAllFilters(task: Task): Boolean {
        return matchesStatusFilter(task) && 
               matchesTypeFilter(task) && 
               matchesSearchQuery(task)
    }

    private fun matchesStatusFilter(task: Task): Boolean {
        return when (currentFilter) {
            TaskFilter.ALL -> true
            TaskFilter.ACTIVE_QUEUE -> task.status in listOf(TaskStatus.RUNNING, TaskStatus.PENDING)
            TaskFilter.COMPLETED -> task.status == TaskStatus.COMPLETED
            TaskFilter.CANCELLED -> task.status == TaskStatus.CANCELLED
            TaskFilter.FAILED -> task.status == TaskStatus.FAILED
        }
    }

    private fun matchesTypeFilter(task: Task): Boolean {
        return selectedTypes.isEmpty() || 
               selectedTypes.contains(getTaskTypeName(task.type))
    }

    private fun matchesSearchQuery(task: Task): Boolean {
        return currentSearchQuery.isEmpty() || 
               searchHelper.matches(task, currentSearchQuery)
    }

    private fun sortTasks(tasks: List<Task>): List<Task> {
        return if (currentFilter == TaskFilter.ACTIVE_QUEUE) {
            sortActiveQueueTasks(tasks)
        } else {
            sortByOrder(tasks)
        }
    }

    private fun sortActiveQueueTasks(tasks: List<Task>): List<Task> {
        return tasks.sortedWith(compareBy(
            { it.status != TaskStatus.RUNNING },
            { it.status != TaskStatus.PENDING },
            { it.createdAt }
        ))
    }

    private fun sortByOrder(tasks: List<Task>): List<Task> {
        return when (currentSortOrder) {
            SortOrder.DATE_DESC -> tasks.sortedByDescending { it.createdAt }
            SortOrder.DATE_ASC -> tasks.sortedBy { it.createdAt }
            SortOrder.NAME_ASC -> tasks.sortedBy { getTaskDisplayName(it) }
            SortOrder.NAME_DESC -> tasks.sortedByDescending { getTaskDisplayName(it) }
            SortOrder.TYPE -> tasks.sortedBy { it.type.name }
            SortOrder.STATUS -> tasks.sortedBy { it.status.name }
        }
    }

    private fun getTaskDisplayName(task: Task): String {
        return task.inputData["profileName"] as? String ?: "작업 ${task.id.take(8)}"
    }

    private fun getTaskTypeName(type: com.ssc.namespring.model.domain.entity.TaskType): String {
        return when (type) {
            com.ssc.namespring.model.domain.entity.TaskType.NAMING -> "작명"
            com.ssc.namespring.model.domain.entity.TaskType.EVALUATION -> "평가"
            com.ssc.namespring.model.domain.entity.TaskType.COMPARISON -> "비교"
            com.ssc.namespring.model.domain.entity.TaskType.REPORT_GENERATION -> "보고서"
        }
    }
}