// ui/history/HistoryActivityLifecycleHandler.kt
package com.ssc.namespring.ui.history

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner

class HistoryActivityLifecycleHandler(
    private val activity: AppCompatActivity,
    private val coordinator: HistoryActivityCoordinator
) {
    fun setupViews() {
        with(coordinator) {
            uiManager.setupViews(
                onTabSelected = { viewModel.setFilter(it) },
                onChipChecked = { viewModel.updateTypeFilters() },
                onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                onSortOrderChanged = { viewModel.setSortOrder(it) },
                onLoadAllClicked = { viewModel.loadAllItems() }
            )

            selectionManager.setupSelectionButtons(
                onDeleteSelected = { 
                    val selectedTasks = viewModel.getSelectedTasks(adapter.getSelectedTaskIds())
                    taskActionHandler.deleteSelectedTasks(selectedTasks) {
                        selectionManager.exitSelectionMode()
                    }
                }
            )
        }
    }

    fun observeData(lifecycleOwner: LifecycleOwner) {
        with(coordinator) {
            viewModel.tasks.observe(lifecycleOwner) { tasks ->
                adapter.submitList(tasks)
                uiManager.updateEmptyView(tasks.isEmpty())
            }

            viewModel.hasMoreData.observe(lifecycleOwner) { hasMore ->
                uiManager.setLoadingMore(!hasMore)
            }
        }
    }
}
