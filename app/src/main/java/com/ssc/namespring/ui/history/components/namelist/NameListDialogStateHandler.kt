// ui/history/components/namelist/NameListDialogStateHandler.kt
package com.ssc.namespring.ui.history.components.namelist

import com.ssc.namespring.ui.history.adapter.NameListAdapter
import com.ssc.namespring.ui.history.viewmodel.NameListUiState
import com.ssc.namingengine.data.GeneratedName

class NameListDialogStateHandler(
    private val viewManager: NameListDialogViewManager,
    private val dataManager: NameListDialogDataManager,
    private val eventHandler: NameListDialogEventHandler,
    private val adapterProvider: () -> NameListAdapter?
) {

    fun handleState(state: NameListUiState) {
        when {
            state.isLoading -> handleLoading()
            state.error != null -> handleError(state.error)
            state.filteredNames.isEmpty() -> handleEmptyNames(state.searchQuery)
            else -> handleContent(state)
        }
    }

    private fun handleLoading() {
        viewManager.showLoading()
    }

    private fun handleError(error: String) {
        viewManager.showEmpty(error)
    }

    private fun handleEmptyNames(searchQuery: String) {
        val message = if (searchQuery.isNotEmpty()) {
            String.format(NameListDialogConstants.SEARCH_NO_RESULT_FORMAT, searchQuery)
        } else {
            NameListDialogConstants.ERROR_NO_NAME_DATA
        }
        viewManager.showEmpty(message)
    }

    private fun handleContent(state: NameListUiState) {
        viewManager.showContent()
        state.task?.let { task ->
            viewManager.setupTitle(task)
            dataManager.updateBirthInfo(task)
        }
        adapterProvider()?.submitList(state.filteredNames) {
            viewManager.recyclerView.scrollToPosition(0)
        }
    }
}