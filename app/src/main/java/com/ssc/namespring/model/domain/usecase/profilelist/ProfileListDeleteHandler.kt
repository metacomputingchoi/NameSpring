// model/domain/usecase/profilelist/ProfileListDeleteHandler.kt
package com.ssc.namespring.model.domain.usecase.profilelist

import android.content.Context
import com.ssc.namespring.model.domain.usecase.ProfileListDeleteManager
import com.ssc.namespring.model.domain.entity.Profile

class ProfileListDeleteHandler(
    private val deleteManager: ProfileListDeleteManager,
    private val uiStateManager: ProfileListUiStateManager,
    private val onDeleteComplete: () -> Unit
) {
    fun deleteSelected(context: Context) {
        val currentState = uiStateManager.getCurrentState() ?: return
        val selectedIds = currentState.selectedIds.toList()
        if (selectedIds.isEmpty()) return

        deleteManager.showDeleteConfirmDialog(
            context, selectedIds, currentState.profiles
        ) {
            uiStateManager.updateSelectionMode(false)
            onDeleteComplete()
        }
    }

    fun deleteProfile(context: Context, profile: Profile) {
        val currentState = uiStateManager.getCurrentState() ?: return
        deleteManager.showDeleteConfirmDialog(
            context, listOf(profile.id), currentState.profiles
        ) {
            onDeleteComplete()
        }
    }
}
