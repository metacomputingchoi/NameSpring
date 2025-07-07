// HistoryActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ssc.namespring.ui.history.*
import com.ssc.namespring.model.presentation.adapter.TaskHistoryAdapter
import com.ssc.namespring.ui.history.helpers.HistoryDialogHelper

class HistoryActivity : AppCompatActivity(), 
    TaskHistoryAdapter.SelectionListener,
    HistorySelectionManager.SelectionModeListener {

    private lateinit var viewModel: HistoryViewModel
    private lateinit var uiManager: HistoryUIManager
    private lateinit var selectionManager: HistorySelectionManager
    private lateinit var taskActionHandler: HistoryTaskActionHandler

    private lateinit var adapter: TaskHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val profileId = intent.getStringExtra("profile_id")

        initializeComponents(profileId)
        setupViews()
        observeData()
    }

    private fun initializeComponents(profileId: String?) {
        viewModel = ViewModelProvider(this, HistoryViewModelFactory(this, profileId))
            .get(HistoryViewModel::class.java)

        adapter = TaskHistoryAdapter(
            onTaskClick = { task ->
                if (selectionManager.isSelectionMode) {
                    adapter.toggleSelection(task.id)
                } else {
                    HistoryDialogHelper.showTaskDetail(supportFragmentManager, task)
                }
            },
            onTaskLongClick = { task ->
                if (!selectionManager.isSelectionMode) {
                    selectionManager.enterSelectionMode()
                    adapter.toggleSelection(task.id)
                }
                true
            },
            onTaskCancel = { taskActionHandler.cancelTask(it) },
            onTaskRetry = { taskActionHandler.retryTask(it) },
            selectionListener = this
        )

        uiManager = HistoryUIManager(this, adapter)
        selectionManager = HistorySelectionManager(this, adapter, uiManager, this)
        taskActionHandler = HistoryTaskActionHandler(this, viewModel)
    }

    private fun setupViews() {
        uiManager.setupViews(
            onTabSelected = { viewModel.setFilter(it) },
            onChipChecked = { viewModel.updateTypeFilters() },
            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
            onSortOrderChanged = { viewModel.setSortOrder(it) },
            onLoadAllClicked = { viewModel.loadAllItems() }
        )

        selectionManager.setupSelectionButtons(
            onDeleteSelected = { 
                val selectedTasks = viewModel.getSelectedTasks(adapter.getSelectedTaskIds())
                taskActionHandler.deleteSelectedTasks(selectedTasks) {
                    selectionManager.exitSelectionMode()
                }
            }
        )
    }

    private fun observeData() {
        viewModel.tasks.observe(this) { tasks ->
            adapter.submitList(tasks)
            uiManager.updateEmptyView(tasks.isEmpty())
        }

        viewModel.hasMoreData.observe(this) { hasMore ->
            uiManager.setLoadingMore(!hasMore)
        }
    }

    override fun onSelectionChanged(selectedCount: Int) {
        selectionManager.updateSelectionUI(selectedCount)
    }

    override fun onSelectionModeChanged(isSelectionMode: Boolean) {
        adapter.isSelectionMode = isSelectionMode
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)
        menu.findItem(R.id.action_select_mode)?.isVisible = !selectionManager.isSelectionMode
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_select_mode -> {
                selectionManager.enterSelectionMode()
                true
            }
            R.id.action_load_all -> {
                viewModel.showLoadAllDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (selectionManager.isSelectionMode) {
            selectionManager.exitSelectionMode()
        } else {
            super.onBackPressed()
        }
    }
}