// ui/history/HistorySelectionManager.kt
package com.ssc.namespring.ui.history

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.ssc.namespring.R
import com.ssc.namespring.model.presentation.adapter.TaskHistoryAdapter

class HistorySelectionManager(
    private val activity: AppCompatActivity,
    private val adapter: TaskHistoryAdapter,
    private val uiManager: HistoryUIManager,
    private val listener: SelectionModeListener
) {
    interface SelectionModeListener {
        fun onSelectionModeChanged(isSelectionMode: Boolean)
    }

    var isSelectionMode = false
        private set

    private lateinit var btnSelectAll: Button
    private lateinit var btnClearSelection: Button
    private lateinit var btnDeleteSelected: Button

    fun setupSelectionButtons(onDeleteSelected: () -> Unit) {
        btnSelectAll = activity.findViewById(R.id.btnSelectAll)
        btnClearSelection = activity.findViewById(R.id.btnClearSelection)
        btnDeleteSelected = activity.findViewById(R.id.btnDeleteSelected)

        btnSelectAll.setOnClickListener {
            toggleSelectAll()
        }

        btnClearSelection.setOnClickListener {
            adapter.clearSelection()
        }

        btnDeleteSelected.setOnClickListener {
            onDeleteSelected()
        }
    }

    fun enterSelectionMode() {
        isSelectionMode = true
        uiManager.selectionModeButtons.visibility = View.VISIBLE
        adjustFabPosition(true)
        activity.supportActionBar?.title = "항목 선택"
        listener.onSelectionModeChanged(true)
    }

    fun exitSelectionMode() {
        adapter.clearSelection()
        isSelectionMode = false
        uiManager.selectionModeButtons.visibility = View.GONE
        adjustFabPosition(false)
        activity.supportActionBar?.title = "작업 기록"
        listener.onSelectionModeChanged(false)
    }

    fun updateSelectionUI(selectedCount: Int) {
        if (isSelectionMode) {
            activity.supportActionBar?.title = "$selectedCount 개 선택됨"
            btnDeleteSelected.isEnabled = selectedCount > 0
            updateSelectAllButton()
        }
    }

    private fun toggleSelectAll() {
        val visibleSelectedCount = adapter.getVisibleSelectedCount()
        val visibleTotalCount = adapter.currentList.size

        if (visibleSelectedCount == visibleTotalCount) {
            adapter.deselectVisible()
        } else {
            adapter.selectVisible()
        }
        updateSelectAllButton()
    }

    private fun updateSelectAllButton() {
        val visibleSelectedCount = adapter.getVisibleSelectedCount()
        val visibleTotalCount = adapter.currentList.size

        btnSelectAll.text = if (visibleSelectedCount == visibleTotalCount && visibleTotalCount > 0) {
            "선택 해제"
        } else {
            "전체 선택"
        }
    }

    private fun adjustFabPosition(isSelectionMode: Boolean) {
        (uiManager.fabScrollTop.layoutParams as CoordinatorLayout.LayoutParams).apply {
            bottomMargin = activity.resources.getDimensionPixelSize(
                if (isSelectionMode) R.dimen.fab_margin_with_selection 
                else R.dimen.fab_margin_normal
            )
        }
    }
}