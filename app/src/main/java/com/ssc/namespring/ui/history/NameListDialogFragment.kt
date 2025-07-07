// ui/history/NameListDialogFragment.kt
package com.ssc.namespring.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.ui.history.adapter.NameListAdapter
import com.ssc.namespring.ui.history.data.NameListDataLoader
import com.ssc.namespring.ui.history.manager.NameSearchManager
import com.ssc.namespring.ui.history.manager.NameSortManager
import com.ssc.namespring.ui.history.view.NameListViewBinder
import com.ssc.namespring.ui.history.viewmodel.NameListViewModel
import com.ssc.namingengine.data.GeneratedName
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    private lateinit var viewBinder: NameListViewBinder
    private lateinit var viewModel: NameListViewModel
    private var adapter: NameListAdapter? = null
    private lateinit var favoriteRepository: FavoriteNameRepository
    private lateinit var taskRepository: TaskRepository

    private val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREAN)
    private var birthDateTime: String = ""
    private var birthDateTimeMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)

        taskRepository = TaskRepository.getInstance(requireContext())
        favoriteRepository = FavoriteNameRepository.getInstance(requireContext())

        viewModel = NameListViewModel(
            taskRepository = taskRepository,
            dataLoader = NameListDataLoader(taskRepository),
            searchManager = NameSearchManager(),
            sortManager = NameSortManager()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_name_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinder = NameListViewBinder(view)
        setupViews()
        observeViewModel()

        val taskId = arguments?.getString(ARG_TASK_ID)
        if (taskId != null) {
            viewModel.loadTask(taskId)
        } else {
            viewBinder.showEmpty("작업 ID가 없습니다.")
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun setupViews() {
        // RecyclerView 설정
        viewBinder.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 검색 설정
        viewBinder.setupSearchView(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchQuery(newText ?: "")
                return true
            }
        })

        // 정렬 설정
        viewBinder.setupSortSpinner { sortOrder ->
            viewModel.updateSortOrder(sortOrder)
        }

        // 버튼 설정
        viewBinder.btnTaskInfo.setOnClickListener {
            viewModel.uiState.value.task?.let {
                TaskDetailDialog(requireContext(), it, taskRepository).show()
            }
        }

        viewBinder.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when {
                        state.isLoading -> viewBinder.showLoading()
                        state.error != null -> viewBinder.showEmpty(state.error)
                        state.filteredNames.isEmpty() -> {
                            val message = if (state.searchQuery.isNotEmpty()) {
                                "'${state.searchQuery}'에 대한 검색 결과가 없습니다"
                            } else {
                                "이름 데이터가 없습니다"
                            }
                            viewBinder.showEmpty(message)
                        }
                        else -> {
                            viewBinder.showContent()
                            state.task?.let { task ->
                                viewBinder.setupTitle(task)
                                updateBirthInfo(task)
                                setupAdapter()
                            }
                            adapter?.submitList(state.filteredNames) {
                                viewBinder.recyclerView.scrollToPosition(0)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateBirthInfo(task: com.ssc.namespring.model.domain.entity.Task) {
        task.inputData["birthDateTime"]?.let { birthDateTimeStr ->
            val millis = (birthDateTimeStr as? String)?.toLongOrNull()
            millis?.let {
                birthDateTimeMillis = it
                birthDateTime = dateFormat.format(Date(it))
            }
        }
    }

    private fun setupAdapter() {
        if (adapter == null) {
            adapter = NameListAdapter(
                birthDateTime = birthDateTime,
                birthDateTimeMillis = birthDateTimeMillis,
                onNameClick = { name -> showNameDetail(name) },
                favoriteRepository = favoriteRepository,
                lifecycleOwner = viewLifecycleOwner
            )
            viewBinder.recyclerView.adapter = adapter
        }
    }

    private fun showNameDetail(name: GeneratedName) {
        NameDetailDialog(requireContext(), name).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinder.recyclerView.adapter = null
        adapter = null
    }
}