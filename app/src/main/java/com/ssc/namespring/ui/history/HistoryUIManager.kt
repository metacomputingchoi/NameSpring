// ui/history/HistoryUIManager.kt
package com.ssc.namespring.ui.history

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ssc.namespring.model.presentation.adapter.TaskHistoryAdapter
import com.ssc.namespring.ui.history.components.HistoryViewFinder
import com.ssc.namespring.ui.history.components.HistoryUISetup

class HistoryUIManager(
    private val activity: Activity,
    private val adapter: TaskHistoryAdapter
) {
    private val viewFinder = HistoryViewFinder(activity)
    private val uiSetup = HistoryUISetup(activity, viewFinder, adapter)

    // 외부에서 접근 가능하도록 getter 추가
    val selectionModeButtons: LinearLayout
        get() = viewFinder.selectionModeButtons

    val fabScrollTop: FloatingActionButton
        get() = viewFinder.fabScrollTop

    fun setupViews(
        onTabSelected: (HistoryFilterManager.TaskFilter) -> Unit,
        onChipChecked: () -> Unit,
        onSearchQueryChanged: (String) -> Unit,
        onSortOrderChanged: (HistoryFilterManager.HistorySortOrder) -> Unit,
        onLoadAllClicked: () -> Unit
    ) {
        uiSetup.apply {
            setupToolbar()
            setupRecyclerView(onLoadAllClicked)
            setupTabs(onTabSelected)
            setupFilters(onChipChecked)
            setupSearch(onSearchQueryChanged)
            setupSort(onSortOrderChanged)
            setupFab()
        }
    }

    fun updateEmptyView(isEmpty: Boolean) {
        viewFinder.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        viewFinder.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    fun setLoadingMore(isLoading: Boolean) {
        // 필요시 로딩 인디케이터 표시
    }
}