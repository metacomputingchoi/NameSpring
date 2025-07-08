// ui/history/HistoryActivityCoordinator.kt
package com.ssc.namespring.ui.history

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ssc.namespring.model.presentation.adapter.TaskHistoryAdapter
import com.ssc.namespring.ui.history.helpers.HistoryDialogHelper

class HistoryActivityCoordinator(
    private val activity: AppCompatActivity,
    private val profileId: String?
) {
    lateinit var viewModel: HistoryViewModel
        private set
    lateinit var adapter: TaskHistoryAdapter
        private set
    lateinit var selectionManager: HistorySelectionManager
        private set
    lateinit var uiManager: HistoryUIManager
        private set
    lateinit var taskActionHandler: HistoryTaskActionHandler
        private set

    fun initialize(
        selectionListener: TaskHistoryAdapter.SelectionListener,
        selectionModeListener: HistorySelectionManager.SelectionModeListener
    ) {
        initializeViewModel()
        initializeAdapter(selectionListener)
        initializeManagers(selectionModeListener)
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(
            activity, 
            HistoryViewModelFactory(activity, profileId)
        ).get(HistoryViewModel::class.java)
    }

    private fun initializeAdapter(selectionListener: TaskHistoryAdapter.SelectionListener) {
        adapter = TaskHistoryAdapter(
            onTaskClick = { task ->
                if (selectionManager.isSelectionMode) {
                    adapter.toggleSelection(task.id)
                } else {
                    HistoryDialogHelper.showTaskDetail(
                        activity.supportFragmentManager, 
                        task
                    )
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
            selectionListener = selectionListener
        )
    }

    private fun initializeManagers(
        selectionModeListener: HistorySelectionManager.SelectionModeListener
    ) {
        uiManager = HistoryUIManager(activity, adapter)
        selectionManager = HistorySelectionManager(
            activity, 
            adapter, 
            uiManager, 
            selectionModeListener
        )
        taskActionHandler = HistoryTaskActionHandler(activity, viewModel)
    }
}
