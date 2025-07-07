// ui/compare/CompareActivityComponents.kt
package com.ssc.namespring.ui.compare

import androidx.appcompat.app.AppCompatActivity

class CompareActivityComponents(
    private val activity: AppCompatActivity
) {
    val initializer = CompareActivityInitializer(activity, activity)
    val viewBinder = CompareActivityViewBinder(activity)

    lateinit var tabManager: CompareTabManager
    lateinit var searchManager: CompareSearchManager
    lateinit var sortManager: CompareSortManager
    lateinit var dragDropHelper: CompareDragDropHelper
    lateinit var uiUpdater: CompareUIUpdater
    lateinit var viewModelObserver: CompareViewModelObserver

    fun initialize() {
        initializer.initialize()
        viewBinder.bindViews()

        tabManager = CompareTabManager(
            viewBinder.tabLayout,
            initializer.viewModel,
            initializer.sourceAdapter
        )

        searchManager = CompareSearchManager(
            activity,
            viewBinder.searchView,
            initializer.viewModel
        )

        sortManager = CompareSortManager(
            activity,
            viewBinder.chipGroupSort,
            initializer.viewModel
        )

        dragDropHelper = CompareDragDropHelper(
            initializer.sourceAdapter,
            initializer.viewModel
        )

        uiUpdater = CompareUIUpdater(
            activity,
            viewBinder.chipGroupFilters,
            viewBinder.fabCompare,
            viewBinder.containerSource,
            viewBinder.containerTarget,
            initializer.viewModel
        )

        viewModelObserver = CompareViewModelObserver(
            activity,
            initializer.viewModel,
            initializer.sourceAdapter,
            initializer.targetAdapter,
            uiUpdater,
            sortManager,
            viewBinder
        )
    }
}