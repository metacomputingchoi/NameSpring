// CompareActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssc.namespring.ui.compare.*
import com.ssc.namespring.ui.compare.filter.FilterBottomSheet

class CompareActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CompareActivity"
    }

    // Components
    private lateinit var initializer: CompareActivityInitializer
    private lateinit var viewBinder: CompareActivityViewBinder
    private lateinit var tabManager: CompareTabManager
    private lateinit var searchManager: CompareSearchManager
    private lateinit var sortManager: CompareSortManager
    private lateinit var dragDropHelper: CompareDragDropHelper
    private lateinit var uiUpdater: CompareUIUpdater
    private lateinit var viewModelObserver: CompareViewModelObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)

        initializeComponents()
        setupViews()
        observeViewModel()
    }

    private fun initializeComponents() {
        // Initialize core components
        initializer = CompareActivityInitializer(this, this)
        initializer.initialize()

        viewBinder = CompareActivityViewBinder(this)
        viewBinder.bindViews()

        // Initialize managers
        tabManager = CompareTabManager(
            viewBinder.tabLayout,
            initializer.viewModel,
            initializer.sourceAdapter
        )

        searchManager = CompareSearchManager(
            this,
            viewBinder.searchView,
            initializer.viewModel
        )

        sortManager = CompareSortManager(
            this,
            viewBinder.chipGroupSort,
            initializer.viewModel
        )

        dragDropHelper = CompareDragDropHelper(
            initializer.sourceAdapter,
            initializer.viewModel
        )

        uiUpdater = CompareUIUpdater(
            this,
            viewBinder.chipGroupFilters,
            viewBinder.fabCompare,
            viewBinder.containerSource,
            viewBinder.containerTarget,
            initializer.viewModel
        )

        viewModelObserver = CompareViewModelObserver(
            this,
            initializer.viewModel,
            initializer.sourceAdapter,
            initializer.targetAdapter,
            uiUpdater,
            sortManager,
            viewBinder
        )
    }

    private fun setupViews() {
        // Setup toolbar
        setSupportActionBar(viewBinder.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "이름 비교"
        }

        // Setup tabs
        tabManager.setupTabs()

        // Setup RecyclerViews
        viewBinder.recyclerViewSource.apply {
            layoutManager = LinearLayoutManager(this@CompareActivity)
            adapter = initializer.sourceAdapter
        }

        viewBinder.recyclerViewTarget.apply {
            layoutManager = LinearLayoutManager(this@CompareActivity)
            adapter = initializer.targetAdapter
        }

        // Setup search
        searchManager.setupSearch()

        // Setup sort chips
        sortManager.setupSortChips()

        // Setup filter button
        viewBinder.btnAddFilter.setOnClickListener {
            showFilterBottomSheet()
        }

        // Setup compare button
        viewBinder.fabCompare.setOnClickListener {
            performComparison()
        }

        // Setup drag and drop
        dragDropHelper.setupDragAndDrop(viewBinder.recyclerViewSource)

        // 비교 모드 초기 설정
        uiUpdater.updateComparisonMode(false)
    }

    private fun observeViewModel() {
        viewModelObserver.observeViewModel()
    }

    private fun showFilterBottomSheet() {
        FilterBottomSheet(this) { filters ->
            filters.forEach { (type, value) ->
                initializer.viewModel.addFilter(type, value)
            }
        }.show()
    }

    private fun performComparison() {
        val comparisonList = initializer.viewModel.comparisonList.value ?: emptyList()
        if (comparisonList.size >= 2) {
            CompareResultDialog(this, comparisonList).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_compare, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_clear_comparison -> {
                initializer.viewModel.clearComparison()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}