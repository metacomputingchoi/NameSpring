// model/domain/usecase/ProfileListManager.kt
package com.ssc.namespring.model.domain.usecase

import android.R
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.google.android.material.snackbar.Snackbar
import com.ssc.namespring.model.presentation.components.ProfileListUiState
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.usecase.profilelist.*

class ProfileListManager {
    private val uiStateManager = ProfileListUiStateManager()
    val uiState: LiveData<ProfileListUiState> = uiStateManager.uiState

    private val loadingManager = ProfileListLoadingManager()
    private val selectionManager = ProfileListSelectionManager()
    private val deleteManager = ProfileListDeleteManager()
    private val sortSearchManager = ProfileListSortSearchManager()

    private val loadHandler = ProfileListLoadHandler(loadingManager, sortSearchManager, uiStateManager)
    private val selectionHandler = ProfileListSelectionHandler(selectionManager, uiStateManager)
    private val deleteHandler = ProfileListDeleteHandler(deleteManager, uiStateManager) { 
        loadHandler.refreshProfiles() 
    }
    private val navigationHandler = ProfileListNavigationHandler(selectionHandler)

    fun loadProfiles() = loadHandler.loadProfiles()
    fun loadMoreProfiles() = loadHandler.loadMoreProfiles()
    fun refreshProfiles() = loadHandler.refreshProfiles()

    fun setSearchQuery(query: String) {
        sortSearchManager.updateSearchQuery(query)
        loadingManager.resetPagination()
        loadProfiles()
    }

    fun setSortType(profileManagerSortType: ProfileManager.ProfileManagerSortType) {
        sortSearchManager.updateSortType(profileManagerSortType)
        loadingManager.resetPagination()
        loadProfiles()
    }

    fun toggleSelectionMode() = selectionHandler.toggleSelectionMode()
    fun enterSelectionMode() = selectionHandler.enterSelectionMode()
    fun exitSelectionMode() = selectionHandler.exitSelectionMode()
    fun toggleSelectAll() = selectionHandler.toggleSelectAll()
    fun toggleSelection(profileId: String) = selectionHandler.toggleSelection(profileId)
    fun isInSelectionMode(): Boolean = selectionHandler.isInSelectionMode()

    fun onProfileClick(context: Context, profile: Profile) = 
        navigationHandler.handleProfileClick(context, profile)

    fun onProfileLongClick(profile: Profile): Boolean = 
        navigationHandler.handleProfileLongClick(profile)

    fun deleteSelected(context: Context) = deleteHandler.deleteSelected(context)
    fun deleteProfile(context: Context, profile: Profile) = deleteHandler.deleteProfile(context, profile)

    fun loadAllProfiles(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("전체 로드")
            .setMessage("모든 프로필을 한 번에 로드하시겠습니까? 프로필이 많을 경우 시간이 걸릴 수 있습니다.")
            .setPositiveButton("로드") { _, _ ->
                val profiles = loadHandler.loadAllAtOnce()
                Snackbar.make(
                    (context as AppCompatActivity).findViewById(R.id.content),
                    "${profiles.size}개의 프로필을 모두 로드했습니다",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
