// model/domain/usecase/ProfileListManager.kt
package com.ssc.namespring.model.domain.usecase

import android.R
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import com.ssc.namespring.MainActivity
import com.ssc.namespring.model.presentation.components.ProfileListUiState
import com.ssc.namespring.model.domain.entity.Profile

class ProfileListManager {
    private val _uiState = MutableLiveData<ProfileListUiState>()
    val uiState: LiveData<ProfileListUiState> = _uiState

    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()
    private val loadingManager = ProfileListLoadingManager()
    private val selectionManager = ProfileListSelectionManager()
    private val deleteManager = ProfileListDeleteManager()
    private val sortSearchManager = ProfileListSortSearchManager()

    init {
        _uiState.value = ProfileListUiState()
    }

    fun loadProfiles() {
        if (_uiState.value?.isLoading == true) return
        updateLoadingState(true)

        val (newProfiles, _) = loadingManager.loadProfiles(
            sortSearchManager.currentQuery,
            sortSearchManager.currentSortType,
            _uiState.value?.profiles ?: emptyList()
        )

        _uiState.value = _uiState.value?.copy(profiles = newProfiles, isLoading = false)
    }

    fun loadMoreProfiles() {
        if (loadingManager.canLoadMore()) {
            loadingManager.startLoadingMore()
            loadingManager.incrementPage()
            loadProfiles()
        }
    }

    fun refreshProfiles() {
        loadingManager.resetPagination()
        _uiState.value = _uiState.value?.copy(profiles = emptyList())
        loadProfiles()
    }

    fun setSearchQuery(query: String) {
        sortSearchManager.updateSearchQuery(query)
        loadingManager.resetPagination()
        loadProfiles()
    }

    fun setSortType(sortType: ProfileManager.SortType) {
        sortSearchManager.updateSortType(sortType)
        loadingManager.resetPagination()
        loadProfiles()
    }

    fun toggleSelectionMode() {
        if (_uiState.value?.isSelectionMode == true) {
            exitSelectionMode()
        } else {
            enterSelectionMode()
        }
    }

    fun enterSelectionMode() {
        _uiState.value = _uiState.value?.copy(isSelectionMode = true, selectedIds = emptySet())
    }

    fun exitSelectionMode() {
        _uiState.value = _uiState.value?.copy(isSelectionMode = false, selectedIds = emptySet())
    }

    fun toggleSelectAll() {
        val currentState = _uiState.value ?: return
        val newSelectedIds = selectionManager.toggleSelectAll(
            currentState.profiles,
            currentState.selectedIds
        )
        _uiState.value = currentState.copy(selectedIds = newSelectedIds)
    }

    fun onProfileClick(context: Context, profile: Profile) {
        if (_uiState.value?.isSelectionMode == true) {
            toggleSelection(profile.id)
        } else {
            profileManager.switchProfile(profile.id)
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    fun onProfileLongClick(profile: Profile): Boolean {
        if (_uiState.value?.isSelectionMode != true) {
            enterSelectionMode()
            toggleSelection(profile.id)
        }
        return true
    }

    fun toggleSelection(profileId: String) {
        val currentState = _uiState.value ?: return
        val newSelectedIds = selectionManager.toggleSelection(currentState.selectedIds, profileId)
        Log.d("ProfileListManager", "New selected IDs: $newSelectedIds")
        _uiState.value = currentState.copy(selectedIds = newSelectedIds)
    }

    fun deleteSelected(context: Context) {
        val selectedIds = _uiState.value?.selectedIds?.toList() ?: emptyList()
        if (selectedIds.isEmpty()) return
        deleteManager.showDeleteConfirmDialog(
            context, selectedIds, _uiState.value?.profiles ?: emptyList()
        ) {
            exitSelectionMode()
            refreshProfiles()
        }
    }

    fun deleteProfile(context: Context, profile: Profile) {
        deleteManager.showDeleteConfirmDialog(
            context, listOf(profile.id), _uiState.value?.profiles ?: emptyList()
        ) {
            refreshProfiles()
        }
    }

    fun loadAllProfiles(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("전체 로드")
            .setMessage("모든 프로필을 한 번에 로드하시겠습니까? 프로필이 많을 경우 시간이 걸릴 수 있습니다.")
            .setPositiveButton("로드") { _, _ ->
                updateLoadingState(true)
                val profiles = loadingManager.loadAllAtOnce(
                    sortSearchManager.currentQuery,
                    sortSearchManager.currentSortType
                )
                _uiState.value = _uiState.value?.copy(profiles = profiles, isLoading = false)
                Snackbar.make(
                    (context as AppCompatActivity).findViewById(R.id.content),
                    "${profiles.size}개의 프로필을 모두 로드했습니다",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("취소") { _, _ -> updateLoadingState(false) }
            .setOnCancelListener { updateLoadingState(false) }
            .show()
    }

    private fun updateLoadingState(isLoading: Boolean) {
        _uiState.value = _uiState.value?.copy(isLoading = isLoading)
    }

    fun isInSelectionMode(): Boolean = _uiState.value?.isSelectionMode == true
}