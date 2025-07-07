// ui/history/HistoryPaginationManager.kt
package com.ssc.namespring.ui.history

import com.ssc.namespring.model.domain.entity.Task

class HistoryPaginationManager {
    companion object {
        private const val PAGE_SIZE = 50
    }

    private var currentPage = 0
    private var isLoadingMore = false
    var hasMoreData = true

    fun reset() {
        currentPage = 0
        isLoadingMore = false
        hasMoreData = true
    }

    fun getInitialItems(allTasks: List<Task>): List<Task> {
        val endIndex = minOf(PAGE_SIZE, allTasks.size)
        val items = if (allTasks.isNotEmpty()) {
            allTasks.subList(0, endIndex)
        } else {
            emptyList()
        }

        hasMoreData = endIndex < allTasks.size
        currentPage = 1

        return items
    }

    fun canLoadMore(): Boolean {
        return !isLoadingMore && hasMoreData
    }

    fun loadMoreItems(allTasks: List<Task>, currentItems: List<Task>): List<Task> {
        if (!canLoadMore()) return currentItems

        isLoadingMore = true
        val startIndex = currentPage * PAGE_SIZE
        val endIndex = minOf(startIndex + PAGE_SIZE, allTasks.size)

        return if (startIndex < allTasks.size) {
            val newItems = allTasks.subList(startIndex, endIndex)
            val updatedItems = currentItems + newItems

            currentPage++
            hasMoreData = endIndex < allTasks.size
            isLoadingMore = false

            updatedItems
        } else {
            hasMoreData = false
            isLoadingMore = false
            currentItems
        }
    }
}