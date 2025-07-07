// ui/compare/CompareActivitySetupHelper.kt
package com.ssc.namespring.ui.compare

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssc.namespring.R
import com.ssc.namespring.ui.compare.filter.FilterBottomSheet

class CompareActivitySetupHelper(
    private val activity: AppCompatActivity,
    private val components: CompareActivityComponents,
    private val eventHandler: CompareActivityEventHandler
) {

    fun setupToolbar() {
        activity.setSupportActionBar(components.viewBinder.toolbar)
        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "이름 비교"
        }
    }

    fun setupRecyclerViews() {
        components.viewBinder.recyclerViewSource.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = components.initializer.sourceAdapter
        }

        components.viewBinder.recyclerViewTarget.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = components.initializer.targetAdapter
        }
    }

    fun setupInteractions() {
        // Setup tabs
        components.tabManager.setupTabs()

        // Setup search
        components.searchManager.setupSearch()

        // Setup sort chips
        components.sortManager.setupSortChips()

        // Setup filter button
        components.viewBinder.btnAddFilter.setOnClickListener {
            eventHandler.onFilterButtonClick()
        }

        // Setup compare button
        components.viewBinder.fabCompare.setOnClickListener {
            eventHandler.onCompareButtonClick()
        }

        // Setup drag and drop
        components.dragDropHelper.setupDragAndDrop(
            components.viewBinder.recyclerViewSource
        )

        // 비교 모드 초기 설정
        components.uiUpdater.updateComparisonMode(false)
    }

    fun setupObservers() {
        components.viewModelObserver.observeViewModel()
    }
}