// ui/history/NameListDialogFragment.kt
package com.ssc.namespring.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.ui.history.adapter.NameListAdapter
import com.ssc.namespring.ui.history.components.namelist.NameListDialogDataManager
import com.ssc.namespring.ui.history.components.namelist.NameListDialogEventHandler
import com.ssc.namespring.ui.history.components.namelist.NameListDialogViewManager
import kotlinx.coroutines.launch

class NameListDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_TASK_ID = "task_id"

        fun newInstance(taskId: String): NameListDialogFragment {
            return NameListDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TASK_ID, taskId)
                }
            }
        }
    }

    private lateinit var viewManager: NameListDialogViewManager
    private lateinit var dataManager: NameListDialogDataManager
    private lateinit var eventHandler: NameListDialogEventHandler
    private var adapter: NameListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)

        val taskRepository = TaskRepository.getInstance(requireContext())
        val favoriteRepository = FavoriteNameRepository.getInstance(requireContext())

        dataManager = NameListDialogDataManager(taskRepository, favoriteRepository)
        eventHandler = NameListDialogEventHandler(requireContext(), taskRepository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_name_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        observeViewModel()

        val taskId = arguments?.getString(ARG_TASK_ID)
        if (taskId != null) {
            dataManager.loadTask(taskId)
        } else {
            viewManager.showEmpty("작업 ID가 없습니다.")
        }
    }

    override fun onStart() {
        super.onStart()
        viewManager.setDialogSize(dialog?.window)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dataManager.viewModel.uiState.collect { state ->
                    when {
                        state.isLoading -> viewManager.showLoading()
                        state.error != null -> viewManager.showEmpty(state.error)
                        state.filteredNames.isEmpty() -> {
                            val message = if (state.searchQuery.isNotEmpty()) {
                                "'${state.searchQuery}'에 대한 검색 결과가 없습니다"
                            } else {
                                "이름 데이터가 없습니다"
                            }
                            viewManager.showEmpty(message)
                        }
                        else -> {
                            viewManager.showContent()
                            state.task?.let { task ->
                                viewManager.setupTitle(task)
                                dataManager.updateBirthInfo(task)
                                setupAdapter()
                            }
                            adapter?.submitList(state.filteredNames) {
                                viewManager.recyclerView.scrollToPosition(0)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupAdapter() {
        if (adapter == null) {
            adapter = NameListAdapter(
                birthDateTime = dataManager.birthDateTime,
                birthDateTimeMillis = dataManager.birthDateTimeMillis,
                onNameClick = { name -> eventHandler.showNameDetail(name) },
                favoriteRepository = dataManager.favoriteRepository,
                lifecycleOwner = viewLifecycleOwner
            )
            viewManager.recyclerView.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewManager.cleanup()
        adapter = null
    }
}