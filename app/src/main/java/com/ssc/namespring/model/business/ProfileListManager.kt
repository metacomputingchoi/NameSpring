// model/business/ProfileListManager.kt
package com.ssc.namespring.model.business

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
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.repository.ProfileManager

class ProfileListViewModel {
    private val _uiState = MutableLiveData<ProfileListUiState>()
    val uiState: LiveData<ProfileListUiState> = _uiState

    private var currentPage = 1
    private val pageSize = 20
    private var isLoadingMore = false
    private var hasMoreData = true
    private var currentSortType = ProfileManager.SortType.DATE_DESC
    private var currentQuery = ""
    private var allProfiles = listOf<Profile>()

    init {
        _uiState.value = ProfileListUiState()
    }

    fun loadProfiles() {
        if (_uiState.value?.isLoading == true) return

        updateLoadingState(true)

        allProfiles = if (currentQuery.isEmpty()) {
            ProfileManager.getSortedProfiles(currentSortType)
        } else {
            ProfileManager.searchProfiles(currentQuery).let { searchResults ->
                when (currentSortType) {
                    ProfileManager.SortType.NAME_ASC -> searchResults.sortedBy { it.profileName }
                    ProfileManager.SortType.NAME_DESC -> searchResults.sortedByDescending { it.profileName }
                    ProfileManager.SortType.SCORE_DESC -> searchResults.sortedByDescending { it.nameBomScore }
                    ProfileManager.SortType.SCORE_ASC -> searchResults.sortedBy { it.nameBomScore }
                    ProfileManager.SortType.DATE_DESC -> searchResults.sortedByDescending { it.createdAt }
                    ProfileManager.SortType.DATE_ASC -> searchResults.sortedBy { it.createdAt }
                }
            }
        }

        val startIndex = (currentPage - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, allProfiles.size)

        val currentProfiles = if (currentPage == 1) {
            allProfiles.subList(0, minOf(pageSize, allProfiles.size))
        } else {
            _uiState.value!!.profiles + allProfiles.subList(startIndex, endIndex)
        }

        hasMoreData = endIndex < allProfiles.size
        isLoadingMore = false

        _uiState.value = _uiState.value?.copy(
            profiles = currentProfiles,
            isLoading = false
        )
    }

    fun loadMoreProfiles() {
        if (!isLoadingMore && hasMoreData) {
            isLoadingMore = true
            currentPage++
            loadProfiles()
        }
    }

    fun refreshProfiles() {
        currentPage = 1
        _uiState.value = _uiState.value?.copy(profiles = emptyList())
        loadProfiles()
    }

    fun setSearchQuery(query: String) {
        currentQuery = query
        currentPage = 1
        loadProfiles()
    }

    fun setSortType(sortType: ProfileManager.SortType) {
        currentSortType = sortType
        currentPage = 1
        loadProfiles()
    }

    fun toggleSelectionMode() {
        val currentState = _uiState.value ?: return
        if (currentState.isSelectionMode) {
            exitSelectionMode()
        } else {
            enterSelectionMode()
        }
    }

    fun enterSelectionMode() {
        _uiState.value = _uiState.value?.copy(
            isSelectionMode = true,
            selectedIds = emptySet()
        )
    }

    fun exitSelectionMode() {
        _uiState.value = _uiState.value?.copy(
            isSelectionMode = false,
            selectedIds = emptySet()
        )
    }

    fun toggleSelectAll() {
        val currentState = _uiState.value ?: return
        val allIds = currentState.profiles.map { it.id }.toSet()

        _uiState.value = if (currentState.selectedIds.size == allIds.size) {
            currentState.copy(selectedIds = emptySet())
        } else {
            currentState.copy(selectedIds = allIds)
        }
    }

    fun onProfileClick(context: Context, profile: Profile) {
        val currentState = _uiState.value ?: return

        if (currentState.isSelectionMode) {
            toggleSelection(profile.id)
        } else {
            ProfileManager.switchProfile(profile.id)
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

        try {
            val newSelectedIds = if (currentState.selectedIds.contains(profileId)) {
                Log.d("ProfileListViewModel", "Deselecting profile: $profileId")
                currentState.selectedIds - profileId
            } else {
                Log.d("ProfileListViewModel", "Selecting profile: $profileId")
                currentState.selectedIds + profileId
            }

            Log.d("ProfileListViewModel", "New selected IDs: $newSelectedIds")
            _uiState.value = currentState.copy(selectedIds = newSelectedIds)
        } catch (e: Exception) {
            Log.e("ProfileListViewModel", "Error toggling selection", e)
        }
    }

    fun deleteSelected(context: Context) {
        Log.d("ProfileListViewModel", "deleteSelected called")
        val selectedIds = _uiState.value?.selectedIds?.toList() ?: emptyList()
        Log.d("ProfileListViewModel", "Selected IDs: $selectedIds")
        Log.d("ProfileListViewModel", "Selected count: ${selectedIds.size}")

        if (selectedIds.isEmpty()) {
            Log.d("ProfileListViewModel", "No items selected")
            return
        }

        showDeleteConfirmDialog(context, selectedIds)
    }

    fun deleteProfile(context: Context, profile: Profile) {
        showDeleteConfirmDialog(context, listOf(profile.id))
    }

    private fun showDeleteConfirmDialog(context: Context, profileIds: List<String>) {
        Log.d("ProfileListViewModel", "showDeleteConfirmDialog called with ${profileIds.size} profiles")

        val message = if (profileIds.size == 1) {
            val profile = _uiState.value?.profiles?.find { it.id == profileIds[0] }
            "'${profile?.profileName}' 프로필을 삭제하시겠습니까?"
        } else {
            "${profileIds.size}개의 프로필을 삭제하시겠습니까?"
        }

        try {
            AlertDialog.Builder(context)
                .setTitle("프로필 삭제")
                .setMessage(message)
                .setPositiveButton("삭제") { _, _ ->
                    Log.d("ProfileListViewModel", "Delete confirmed")
                    // 실제 삭제 수행
                    ProfileManager.deleteProfiles(profileIds)
                    Log.d("ProfileListViewModel", "ProfileManager.deleteProfiles called")

                    // 선택 모드 종료
                    exitSelectionMode()
                    Log.d("ProfileListViewModel", "Selection mode exited")

                    // 프로필 목록 새로고침
                    refreshProfiles()
                    Log.d("ProfileListViewModel", "Profiles refreshed")

                    // 스낵바 메시지 표시
                    val snackbarMessage = if (profileIds.size == 1) {
                        "프로필이 삭제되었습니다"
                    } else {
                        "${profileIds.size}개의 프로필이 삭제되었습니다"
                    }

                    Snackbar.make(
                        (context as AppCompatActivity).findViewById(R.id.content),
                        snackbarMessage,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("취소", null)
                .show()

            Log.d("ProfileListViewModel", "Dialog shown")
        } catch (e: Exception) {
            Log.e("ProfileListViewModel", "Error showing dialog", e)
        }
    }

    fun loadAllProfiles(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("전체 로드")
            .setMessage("모든 프로필을 한 번에 로드하시겠습니까? 프로필이 많을 경우 시간이 걸릴 수 있습니다.")
            .setPositiveButton("로드") { _, _ ->
                updateLoadingState(true)

                val profiles = if (currentQuery.isEmpty()) {
                    ProfileManager.getSortedProfiles(currentSortType)
                } else {
                    ProfileManager.searchProfiles(currentQuery)
                }

                hasMoreData = false
                _uiState.value = _uiState.value?.copy(
                    profiles = profiles,
                    isLoading = false
                )

                Snackbar.make(
                    (context as AppCompatActivity).findViewById(R.id.content),
                    "${profiles.size}개의 프로필을 모두 로드했습니다",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("취소") { _, _ ->
                updateLoadingState(false)
            }
            .setOnCancelListener {
                updateLoadingState(false)
            }
            .show()
    }

    private fun updateLoadingState(isLoading: Boolean) {
        _uiState.value = _uiState.value?.copy(isLoading = isLoading)
    }

    fun isInSelectionMode(): Boolean = _uiState.value?.isSelectionMode ?: false

}

data class ProfileListUiState(
    val profiles: List<Profile> = emptyList(),
    val isLoading: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet()
)
