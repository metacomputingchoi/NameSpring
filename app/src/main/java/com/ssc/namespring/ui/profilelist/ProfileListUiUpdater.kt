// ui/profilelist/ProfileListUiUpdater.kt
package com.ssc.namespring.ui.profilelist

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import com.ssc.namespring.ProfileListActivity
import com.ssc.namespring.model.presentation.adapter.ProfileAdapter
import com.ssc.namespring.model.presentation.components.ProfileListUiState
import com.ssc.namespring.utils.ui.ViewUtils

class ProfileListUiUpdater(
    private val activity: ProfileListActivity,
    private val viewHolder: ProfileListViewHolder
) {

    @SuppressLint("SetTextI18n")
    fun updateUI(state: ProfileListUiState, adapter: ProfileAdapter) {
        Log.d("ProfileListActivity", "updateUI: isSelectionMode=${state.isSelectionMode}, selectedCount=${state.selectedIds.size}")

        adapter.submitList(state.profiles, state.isSelectionMode, state.selectedIds)

        viewHolder.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        viewHolder.tvProfileCount.text = "총 ${state.profiles.size}개"
        viewHolder.tvSelectedCount.text = "${state.selectedIds.size}개 선택됨"

        ViewUtils.updateSelectionModeUI(
            state.isSelectionMode,
            viewHolder.searchView,
            viewHolder.chipGroup,
            viewHolder.tvSelectedCount,
            viewHolder.bottomActionBar,
            viewHolder.fabAdd,
            viewHolder.fabSelectAll,
            state.selectedIds.size,
            adapter.getSelectableItemCount()
        )

        ViewUtils.updateEmptyView(
            viewHolder.recyclerView,
            viewHolder.emptyView,
            viewHolder.fabAdd,
            state.profiles.isEmpty(),
            state.isSelectionMode
        )

        activity.invalidateOptionsMenu()

        Log.d("ProfileListActivity", "bottomActionBar visibility after update: ${viewHolder.bottomActionBar.visibility}")
    }
}