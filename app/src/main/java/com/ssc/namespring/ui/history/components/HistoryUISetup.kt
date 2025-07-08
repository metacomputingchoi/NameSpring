// ui/history/components/HistoryUISetup.kt
package com.ssc.namespring.ui.history.components

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.model.presentation.adapter.TaskHistoryAdapter
import com.ssc.namespring.ui.history.HistoryFilterManager
import com.ssc.namespring.ui.history.helpers.*

class HistoryUISetup(
    private val activity: Activity,
    private val viewFinder: HistoryViewFinder,
    private val adapter: TaskHistoryAdapter
) {
    fun setupToolbar() {
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(viewFinder.toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = "작업 기록"
            }
        }
    }

    fun setupRecyclerView(onLoadMore: () -> Unit) {
        val layoutManager = LinearLayoutManager(activity)
        viewFinder.recyclerView.layoutManager = layoutManager
        viewFinder.recyclerView.adapter = adapter

        viewFinder.recyclerView.addOnScrollListener(
            HistoryScrollListener(layoutManager, viewFinder.fabScrollTop, onLoadMore)
        )
    }

    fun setupTabs(onTabSelected: (HistoryFilterManager.TaskFilter) -> Unit) {
        HistoryTabHelper.setupTabs(viewFinder.tabLayout, onTabSelected)
    }

    fun setupFilters(onChipChecked: () -> Unit) {
        TaskType.values().forEach { type ->
            val chip = HistoryChipHelper.createTypeChip(activity, type, onChipChecked)
            viewFinder.chipGroup.addView(chip)
        }
    }

    fun setupSearch(onQueryChanged: (String) -> Unit) {
        HistorySearchHelper.setupSearchView(activity, viewFinder.searchView, onQueryChanged)
    }

    fun setupSort(onSortChanged: (HistoryFilterManager.HistorySortOrder) -> Unit) {
        HistorySortHelper.setupSortSpinner(activity, viewFinder.sortSpinner, onSortChanged)
    }

    fun setupFab() {
        viewFinder.fabScrollTop.setOnClickListener {
            viewFinder.recyclerView.smoothScrollToPosition(0)
        }
    }
}