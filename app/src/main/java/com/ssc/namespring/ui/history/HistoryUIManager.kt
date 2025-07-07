// app/src/main/java/com/ssc/namespring/ui/history/HistoryUIManager.kt
package com.ssc.namespring.ui.history

import android.app.Activity
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.model.presentation.adapter.TaskHistoryAdapter
import com.ssc.namespring.ui.history.helpers.HistoryChipHelper
import com.ssc.namespring.ui.history.helpers.HistoryScrollListener
import com.ssc.namespring.ui.history.helpers.HistorySearchHelper
import com.ssc.namespring.ui.history.helpers.HistorySortHelper
import com.ssc.namespring.ui.history.helpers.HistoryTabHelper

class HistoryUIManager(
    private val activity: Activity,
    private val adapter: TaskHistoryAdapter
) {
    lateinit var toolbar: Toolbar
    lateinit var tabLayout: TabLayout
    lateinit var chipGroup: ChipGroup
    lateinit var recyclerView: RecyclerView
    lateinit var emptyView: TextView
    lateinit var searchView: SearchView
    lateinit var sortSpinner: Spinner
    lateinit var fabScrollTop: FloatingActionButton
    lateinit var selectionModeButtons: LinearLayout

    fun setupViews(
        onTabSelected: (HistoryFilterManager.TaskFilter) -> Unit,
        onChipChecked: () -> Unit,
        onSearchQueryChanged: (String) -> Unit,
        onSortOrderChanged: (HistoryFilterManager.SortOrder) -> Unit,
        onLoadAllClicked: () -> Unit
    ) {
        findViews()
        setupToolbar()
        setupRecyclerView(onLoadAllClicked)
        setupTabs(onTabSelected)
        setupFilters(onChipChecked)
        setupSearch(onSearchQueryChanged)
        setupSort(onSortOrderChanged)
        setupFab()
    }

    private fun findViews() {
        toolbar = activity.findViewById(R.id.toolbar)
        tabLayout = activity.findViewById(R.id.tabLayout)
        chipGroup = activity.findViewById(R.id.chipGroup)
        recyclerView = activity.findViewById(R.id.recyclerView)
        emptyView = activity.findViewById(R.id.emptyView)
        searchView = activity.findViewById(R.id.searchView)
        sortSpinner = activity.findViewById(R.id.sortSpinner)
        fabScrollTop = activity.findViewById(R.id.fabScrollTop)
        selectionModeButtons = activity.findViewById(R.id.selectionModeButtons)
    }

    private fun setupToolbar() {
        (activity as? androidx.appcompat.app.AppCompatActivity)?.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = "작업 기록"
            }
        }
    }

    private fun setupRecyclerView(onLoadMore: () -> Unit) {
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(HistoryScrollListener(
            layoutManager, 
            fabScrollTop,
            onLoadMore
        ))
    }

    private fun setupTabs(onTabSelected: (HistoryFilterManager.TaskFilter) -> Unit) {
        HistoryTabHelper.setupTabs(tabLayout, onTabSelected)
    }

    private fun setupFilters(onChipChecked: () -> Unit) {
        TaskType.values().forEach { type ->
            val chip = HistoryChipHelper.createTypeChip(activity, type, onChipChecked)
            chipGroup.addView(chip)
        }
    }

    private fun setupSearch(onQueryChanged: (String) -> Unit) {
        HistorySearchHelper.setupSearchView(activity, searchView, onQueryChanged)
    }

    private fun setupSort(onSortChanged: (HistoryFilterManager.SortOrder) -> Unit) {
        HistorySortHelper.setupSortSpinner(activity, sortSpinner, onSortChanged)
    }

    private fun setupFab() {
        fabScrollTop.setOnClickListener {
            recyclerView.smoothScrollToPosition(0)
        }
    }

    fun updateEmptyView(isEmpty: Boolean) {
        emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    fun setLoadingMore(isLoading: Boolean) {
        // 필요시 로딩 인디케이터 표시
    }
}