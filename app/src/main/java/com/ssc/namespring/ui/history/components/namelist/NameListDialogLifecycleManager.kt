// ui/history/components/namelist/NameListDialogLifecycleManager.kt
package com.ssc.namespring.ui.history.components.namelist

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.ssc.namespring.ui.history.adapter.NameListAdapter
import com.ssc.namespring.ui.history.viewmodel.NameListUiState
import com.ssc.namingengine.data.GeneratedName
import kotlinx.coroutines.launch

class NameListDialogLifecycleManager(
    private val fragment: DialogFragment,
    private val dataManager: NameListDialogDataManager,
    private val eventHandler: NameListDialogEventHandler
) {

    fun createAdapter(lifecycleOwner: LifecycleOwner): NameListAdapter {
        return NameListAdapter(
            birthDateTime = dataManager.birthDateTime,
            birthDateTimeMillis = dataManager.birthDateTimeMillis,
            onNameClick = { name -> eventHandler.showNameDetail(name) },
            favoriteRepository = dataManager.favoriteRepository,
            lifecycleOwner = lifecycleOwner
        )
    }

    fun loadTaskFromArguments(arguments: Bundle?) {
        val taskId = arguments?.getString(NameListDialogConstants.ARG_TASK_ID)
        if (taskId != null) {
            dataManager.loadTask(taskId)
        } else {
            throw IllegalArgumentException(NameListDialogConstants.ERROR_NO_TASK_ID)
        }
    }

    fun observeViewModel(
        lifecycleOwner: LifecycleOwner,
        stateHandler: NameListDialogStateHandler
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dataManager.viewModel.uiState.collect { state ->
                    stateHandler.handleState(state)
                }
            }
        }
    }
}