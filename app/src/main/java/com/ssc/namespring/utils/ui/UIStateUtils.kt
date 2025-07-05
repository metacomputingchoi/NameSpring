// utils/ui/UIStateUtils.kt
package com.ssc.namespring.utils.ui

import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ssc.namespring.R

object UIStateUtils {

    fun updateSelectionModeUI(
        isSelectionMode: Boolean,
        searchView: SearchView,
        chipGroup: ChipGroup,
        tvSelectedCount: TextView,
        bottomActionBar: LinearLayout,
        fabAdd: FloatingActionButton,
        fabSelectAll: ExtendedFloatingActionButton,
        selectedCount: Int,
        totalCount: Int
    ) {
        Log.d("UIStateUtils", "updateSelectionModeUI: isSelectionMode=$isSelectionMode, selectedCount=$selectedCount")

        searchView.isVisible = !isSelectionMode
        chipGroup.isVisible = !isSelectionMode
        tvSelectedCount.isVisible = isSelectionMode

        bottomActionBar.isVisible = isSelectionMode
        Log.d("UIStateUtils", "bottomActionBar.isVisible = ${bottomActionBar.isVisible}")
        Log.d("UIStateUtils", "bottomActionBar.visibility = ${bottomActionBar.visibility}")

        if (isSelectionMode) {
            val deleteButton = bottomActionBar.findViewById<Button>(R.id.btnDeleteSelected)
            deleteButton?.isEnabled = selectedCount > 0
            Log.d("UIStateUtils", "Delete button enabled: ${deleteButton?.isEnabled}, selectedCount: $selectedCount")

            fabAdd.hide()
            fabSelectAll.show()

            if (selectedCount == totalCount && totalCount > 0) {
                fabSelectAll.text = "전체 해제"
                fabSelectAll.setIconResource(R.drawable.ic_clear)
            } else {
                fabSelectAll.text = "전체 선택"
                fabSelectAll.setIconResource(R.drawable.ic_check)
            }
        } else {
            fabAdd.show()
            fabSelectAll.hide()
        }
    }

    fun updateEmptyView(
        recyclerView: RecyclerView,
        emptyView: LinearLayout,
        fabAdd: FloatingActionButton,
        isEmpty: Boolean,
        isSelectionMode: Boolean
    ) {
        if (isEmpty) {
            recyclerView.isVisible = false
            emptyView.isVisible = true
            fabAdd.hide()
        } else {
            recyclerView.isVisible = true
            emptyView.isVisible = false
            if (!isSelectionMode) {
                fabAdd.show()
            }
        }
    }
}