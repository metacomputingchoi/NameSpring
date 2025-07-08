// model/domain/usecase/profilelist/ProfileListSelectionHandler.kt
package com.ssc.namespring.model.domain.usecase.profilelist

import android.util.Log
import com.ssc.namespring.model.domain.usecase.ProfileListSelectionManager
import com.ssc.namespring.model.domain.entity.Profile

class ProfileListSelectionHandler(
    private val selectionManager: ProfileListSelectionManager,
    private val uiStateManager: ProfileListUiStateManager
) {
    fun toggleSelectionMode() {
        val currentState = uiStateManager.getCurrentState() ?: return
        if (currentState.isSelectionMode) {
            exitSelectionMode()
        } else {
            enterSelectionMode()
        }
    }

    fun enterSelectionMode() {
        uiStateManager.updateSelectionMode(true, emptySet())
    }

    fun exitSelectionMode() {
        uiStateManager.updateSelectionMode(false, emptySet())
    }

    fun toggleSelectAll() {
        val currentState = uiStateManager.getCurrentState() ?: return
        val newSelectedIds = selectionManager.toggleSelectAll(
            currentState.profiles,
            currentState.selectedIds
        )
        uiStateManager.updateSelectedIds(newSelectedIds)
    }

    fun toggleSelection(profileId: String) {
        val currentState = uiStateManager.getCurrentState() ?: return
        val newSelectedIds = selectionManager.toggleSelection(currentState.selectedIds, profileId)
        Log.d("ProfileListSelectionHandler", "New selected IDs: $newSelectedIds")
        uiStateManager.updateSelectedIds(newSelectedIds)
    }

    fun isInSelectionMode(): Boolean = uiStateManager.getCurrentState()?.isSelectionMode == true

    fun getSelectedIds(): Set<String> = uiStateManager.getCurrentState()?.selectedIds ?: emptySet()
}
