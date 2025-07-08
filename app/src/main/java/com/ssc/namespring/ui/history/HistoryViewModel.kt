// ui/history/HistoryViewModel.kt
package com.ssc.namespring.ui.history

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.*
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.model.domain.entity.Task

class HistoryViewModel(
    private val context: Context,
    private val taskRepository: TaskRepository,
    private val profileId: String?
) : ViewModel() {

    private val filterManager = HistoryFilterManager()
    private val paginationManager = HistoryPaginationManager()

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    private val _hasMoreData = MutableLiveData<Boolean>()
    val hasMoreData: LiveData<Boolean> = _hasMoreData

    private var allTasks: List<Task> = emptyList()
    private var filteredTasks: List<Task> = emptyList()

    init {
        observeTaskHistory()
    }

    private fun observeTaskHistory() {
        val liveData = if (profileId != null) {
            taskRepository.taskHistoryMap.map { historyMap ->
                historyMap[profileId]?.tasks ?: emptyList()
            }
        } else {
            taskRepository.taskHistoryMap.map { historyMap ->
                historyMap.values.flatMap { it.tasks }
            }
        }

        liveData.observeForever { tasks ->
            allTasks = tasks
            applyFiltersAndSort()
        }
    }

    fun setFilter(filter: HistoryFilterManager.TaskFilter) {
        filterManager.currentFilter = filter
        paginationManager.reset()
        applyFiltersAndSort()
    }

    fun updateTypeFilters() {
        paginationManager.reset()
        applyFiltersAndSort()
    }

    fun setSearchQuery(query: String) {
        filterManager.currentSearchQuery = query
        paginationManager.reset()
        applyFiltersAndSort()
    }

    fun setSortOrder(order: HistoryFilterManager.HistorySortOrder) {
        filterManager.currentHistorySortOrder = order
        paginationManager.reset()
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        filteredTasks = filterManager.applyFiltersAndSort(allTasks)
        loadInitialItems()
    }

    private fun loadInitialItems() {
        val items = paginationManager.getInitialItems(filteredTasks)
        _tasks.value = items
        _hasMoreData.value = paginationManager.hasMoreData
    }

    fun loadMoreItems() {
        if (paginationManager.canLoadMore()) {
            val newItems = paginationManager.loadMoreItems(filteredTasks, _tasks.value ?: emptyList())
            _tasks.value = newItems
            _hasMoreData.value = paginationManager.hasMoreData
        }
    }

    fun loadAllItems() {
        _tasks.value = filteredTasks
        paginationManager.hasMoreData = false
        _hasMoreData.value = false
    }

    fun showLoadAllDialog() {
        val currentSize = _tasks.value?.size ?: 0
        if (filteredTasks.size > currentSize) {
            AlertDialog.Builder(context)
                .setTitle("전체 로드")
                .setMessage("${filteredTasks.size}개의 항목을 모두 로드하시겠습니까?")
                .setPositiveButton("로드") { _, _ -> loadAllItems() }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    fun getSelectedTasks(selectedIds: Set<String>): List<Task> {
        return allTasks.filter { it.id in selectedIds }
    }
}