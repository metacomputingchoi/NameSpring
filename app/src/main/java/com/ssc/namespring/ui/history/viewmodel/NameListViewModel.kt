// ui/history/viewmodel/NameListViewModel.kt
package com.ssc.namespring.ui.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.ui.history.data.NameListRawRetLoader
import com.ssc.namespring.ui.history.managers.NameSearchManager
import com.ssc.namespring.ui.history.managers.NameSortManager
import com.ssc.namingengine.data.GeneratedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NameListViewModel(
    private val taskRepository: TaskRepository,
    private val dataLoader: NameListRawRetLoader,
    private val searchManager: NameSearchManager,
    private val sortManager: NameSortManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NameListUiState())
    val uiState: StateFlow<NameListUiState> = _uiState

    private var allNames: List<GeneratedName> = emptyList()

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val task = taskRepository.getTask(taskId)
                if (task != null) {
                    _uiState.value = _uiState.value.copy(task = task)
                    loadNames(task)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "작업을 찾을 수 없습니다."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "작업 로드 중 오류가 발생했습니다."
                )
            }
        }
    }

    private suspend fun loadNames(task: Task) {
        try {
            allNames = dataLoader.loadNames(task)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                allNames = allNames,
                filteredNames = allNames
            )
            applyFilterAndSort()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "데이터 로드 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilterAndSort()
    }

    fun updateSortOrder(nameSortOrder: NameSortManager.NameSortOrder) {
        _uiState.value = _uiState.value.copy(nameSortOrder = nameSortOrder)
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        val filtered = searchManager.filter(allNames, _uiState.value.searchQuery)
        val sorted = sortManager.sort(filtered, _uiState.value.nameSortOrder)
        _uiState.value = _uiState.value.copy(filteredNames = sorted)
    }
}

data class NameListUiState(
    val isLoading: Boolean = false,
    val task: Task? = null,
    val allNames: List<GeneratedName> = emptyList(),
    val filteredNames: List<GeneratedName> = emptyList(),
    val searchQuery: String = "",
    val nameSortOrder: NameSortManager.NameSortOrder = NameSortManager.NameSortOrder.SCORE_DESC,
    val error: String? = null
)