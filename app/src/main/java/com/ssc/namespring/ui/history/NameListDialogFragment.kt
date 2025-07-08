// ui/history/NameListDialogFragment.kt
package com.ssc.namespring.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.ui.history.adapter.NameListAdapter
import com.ssc.namespring.ui.history.components.namelist.*

class NameListDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(taskId: String): NameListDialogFragment {
            return NameListDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(NameListDialogConstants.ARG_TASK_ID, taskId)
                }
            }
        }
    }

    private lateinit var viewManager: NameListDialogViewManager
    private lateinit var dataManager: NameListDialogDataManager
    private lateinit var eventHandler: NameListDialogEventHandler
    private lateinit var lifecycleManager: NameListDialogLifecycleManager
    private lateinit var stateHandler: NameListDialogStateHandler
    private var adapter: NameListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
        initializeManagers()
    }

    private fun initializeManagers() {
        val taskRepository = TaskRepository.getInstance(requireContext())
        val favoriteRepository = FavoriteNameRepository.getInstance(requireContext())

        dataManager = NameListDialogDataManager(taskRepository, favoriteRepository)
        eventHandler = NameListDialogEventHandler(requireContext(), taskRepository)
        lifecycleManager = NameListDialogLifecycleManager(this, dataManager, eventHandler)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_name_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewManager(view)
        setupStateHandler()

        try {
            lifecycleManager.loadTaskFromArguments(arguments)
            lifecycleManager.observeViewModel(viewLifecycleOwner, stateHandler)
        } catch (e: IllegalArgumentException) {
            viewManager.showEmpty(e.message ?: NameListDialogConstants.ERROR_NO_TASK_ID)
        }
    }

    private fun setupViewManager(view: View) {
        viewManager = NameListDialogViewManager(
            rootView = view,
            onSearchQueryChanged = { dataManager.updateSearchQuery(it) },
            onSortOrderChanged = { dataManager.updateSortOrder(it) },
            onTaskInfoClicked = {
                dataManager.viewModel.uiState.value.task?.let {
                    eventHandler.showTaskDetail(it)
                }
            },
            onCloseClicked = { dismiss() }
        )
        viewManager.setupViews()
    }

    private fun setupStateHandler() {
        stateHandler = NameListDialogStateHandler(
            viewManager = viewManager,
            dataManager = dataManager,
            eventHandler = eventHandler,
            adapterProvider = {
                if (adapter == null) {
                    adapter = lifecycleManager.createAdapter(viewLifecycleOwner)
                    viewManager.recyclerView.adapter = adapter
                }
                adapter
            }
        )
    }

    override fun onStart() {
        super.onStart()
        viewManager.setDialogSize(dialog?.window)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewManager.cleanup()
        adapter = null
    }
}