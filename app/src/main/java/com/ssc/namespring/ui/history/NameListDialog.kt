// ui/history/NameListDialog.kt
package com.ssc.namespring.ui.history

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.ui.history.managers.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NameListDialog(
    private val activity: AppCompatActivity,
    private val task: Task,
    private val taskRepository: TaskRepository
) : Dialog(activity, android.R.style.Theme_Material_Light_Dialog) {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var viewBinder: NameListRecyclerViewBinder
    private lateinit var dataLoader: NameListDataLoader
    private lateinit var searchManager: NameListSearchManager
    private lateinit var sortManager: NameListSortManager
    private lateinit var uiStateManager: NameListUIStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_name_list)

        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        setupManagers()
        viewBinder.setupViews()
        loadData()
    }

    private fun setupManagers() {
        viewBinder = NameListRecyclerViewBinder(this, activity, task)
        uiStateManager = NameListUIStateManager()

        dataLoader = NameListDataLoader(
            task = task,
            taskRepository = taskRepository,
            scope = scope,
            onDataLoaded = { names ->
                uiStateManager.updateData(names)
                applyFilterAndSort()
            },
            onError = { message ->
                viewBinder.showError(message)
            }
        )

        searchManager = NameListSearchManager { query ->
            uiStateManager.updateSearchQuery(query)
            applyFilterAndSort()
        }

        sortManager = NameListSortManager { order ->
            uiStateManager.updateSortOrder(order)
            applyFilterAndSort()
        }

        viewBinder.setManagers(searchManager, sortManager)
        viewBinder.onTaskInfoClick = {
            TaskDetailDialog(context, task, taskRepository).show()
        }
        viewBinder.onCloseClick = {
            cleanupAndDismiss()
        }
        viewBinder.onNameClick = { name ->
            NameDetailDialog(context, name).show()
        }
    }

    private fun loadData() {
        viewBinder.showLoading()
        dataLoader.loadData()
    }

    private fun applyFilterAndSort() {
        val filteredAndSorted = uiStateManager.getFilteredAndSortedNames(
            searchManager.getSearchHelper()
        )
        viewBinder.updateList(filteredAndSorted, uiStateManager.currentSearchQuery)
    }

    private fun cleanupAndDismiss() {
        dataLoader.cancel()
        job.cancel()
        viewBinder.cleanup()
        dismiss()
    }

    override fun onBackPressed() {
        cleanupAndDismiss()
    }

    override fun dismiss() {
        job.cancel()
        super.dismiss()
    }
}