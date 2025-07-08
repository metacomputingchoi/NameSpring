// model/domain/usecase/profilelist/ProfileListUiStateManager.kt
package com.ssc.namespring.model.domain.usecase.profilelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.presentation.components.ProfileListUiState

class ProfileListUiStateManager : IProfileListUiStateManager {
    private val _uiState = MutableLiveData<ProfileListUiState>()
    val uiState: LiveData<ProfileListUiState> = _uiState

    init {
        _uiState.value = ProfileListUiState()
    }

    override fun updateUiState(update: (ProfileListUiState) -> ProfileListUiState) {
        _uiState.value?.let { currentState ->
            _uiState.value = update(currentState)
        }
    }

    override fun getCurrentState(): ProfileListUiState? = _uiState.value

    fun updateLoadingState(isLoading: Boolean) {
        updateUiState { it.copy(isLoading = isLoading) }
    }

    fun updateProfiles(profiles: List<com.ssc.namespring.model.domain.entity.Profile>) {
        updateUiState { it.copy(profiles = profiles, isLoading = false) }
    }

    fun updateSelectionMode(isSelectionMode: Boolean, selectedIds: Set<String> = emptySet()) {
        updateUiState { it.copy(isSelectionMode = isSelectionMode, selectedIds = selectedIds) }
    }

    fun updateSelectedIds(selectedIds: Set<String>) {
        updateUiState { it.copy(selectedIds = selectedIds) }
    }
}
