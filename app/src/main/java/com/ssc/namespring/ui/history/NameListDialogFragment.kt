// ui/history/NameListDialogFragment.kt 수정
package com.ssc.namespring.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskStatus
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.ui.history.adapter.NameListAdapter
import com.ssc.namespring.utils.search.NameSearchHelper
import com.ssc.namingengine.data.GeneratedName
import kotlinx.coroutines.*
import java.text.Collator
import java.text.SimpleDateFormat
import java.util.*

class NameListDialogFragment : DialogFragment() {

    companion object {
        private const val TAG = "NameListDialogFragment"
        private const val ARG_TASK_ID = "task_id"

        fun newInstance(taskId: String): NameListDialogFragment {
            return NameListDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TASK_ID, taskId)
                }
            }
        }
    }

    // Views
    private lateinit var searchView: SearchView
    private lateinit var sortSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var loadingView: ProgressBar
    private lateinit var btnTaskInfo: MaterialButton

    // Data
    private var task: Task? = null
    private lateinit var taskRepository: TaskRepository
    private lateinit var adapter: NameListAdapter
    private lateinit var favoriteRepository: FavoriteNameRepository

    private var birthDateTimeMillis: Long = 0L
    private var birthDateTime: String = ""

    // Utils
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREAN)
    private val koreanCollator = Collator.getInstance(Locale.KOREAN)
    private val searchHelper = NameSearchHelper()

    // Data lists
    private var allNames: List<GeneratedName> = emptyList()
    private var filteredNames: List<GeneratedName> = emptyList()
    private var currentSearchQuery = ""
    private var currentSortOrder = SortOrder.SCORE_DESC

    // Coroutine
    private var loadJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)

        taskRepository = TaskRepository.getInstance(requireContext())
        favoriteRepository = FavoriteNameRepository.getInstance(requireContext())
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

        val taskId = arguments?.getString(ARG_TASK_ID)
        if (taskId != null) {
            loadTask(taskId)
        } else {
            showError("작업 ID가 없습니다.")
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun loadTask(taskId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Task 가져오기
                val loadedTask = withContext(Dispatchers.IO) {
                    taskRepository.getTask(taskId)
                }

                if (loadedTask != null) {
                    task = loadedTask
                    setupViews(requireView())
                    loadData()
                } else {
                    showError("작업을 찾을 수 없습니다.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading task", e)
                showError("작업 로드 중 오류가 발생했습니다.")
            }
        }
    }

    private fun setupViews(view: View) {
        val currentTask = task ?: return

        // View 초기화
        searchView = view.findViewById(R.id.searchView)
        sortSpinner = view.findViewById(R.id.sortSpinner)
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        loadingView = view.findViewById(R.id.loadingView)
        btnTaskInfo = view.findViewById(R.id.btnTaskInfo)

        // 타이틀 설정
        view.findViewById<TextView>(R.id.tvTitle).text =
            "${currentTask.inputData["profileName"] ?: "작업"} - ${getTaskTypeName(currentTask.type)} 결과"

        // 생년월일시 정보 추출
        currentTask.inputData["birthDateTime"]?.let { birthDateTimeStr ->
            val millis = (birthDateTimeStr as? String)?.toLongOrNull()
            millis?.let {
                birthDateTimeMillis = it
                birthDateTime = dateFormat.format(Date(it))
            }
        }

        // RecyclerView 설정
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NameListAdapter(
            birthDateTime = birthDateTime,
            birthDateTimeMillis = birthDateTimeMillis,
            onNameClick = { name ->
                showNameDetail(name)
            },
            favoriteRepository = favoriteRepository,
            lifecycleOwner = viewLifecycleOwner
        )
        recyclerView.adapter = adapter

        // 검색 설정
        setupSearch()

        // 정렬 설정
        setupSort()

        // 버튼 설정
        btnTaskInfo.setOnClickListener {
            task?.let { TaskDetailDialog(requireContext(), it, taskRepository).show() }
        }

        view.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            dismiss()
        }
    }

    private fun setupSearch() {
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText?.apply {
            setTextColor(requireContext().getColor(R.color.text_primary))
            setHintTextColor(requireContext().getColor(R.color.text_secondary))
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                Log.d(TAG, "Search query changed: $currentSearchQuery")
                applyFilterAndSort()
                return true
            }
        })
    }

    private fun setupSort() {
        val sortOptions = arrayOf(
            "점수 높은순",
            "점수 낮은순",
            "이름순 (가→하)",
            "이름순 (하→가)"
        )

        sortSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sortOptions
        )

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newSortOrder = when (position) {
                    0 -> SortOrder.SCORE_DESC
                    1 -> SortOrder.SCORE_ASC
                    2 -> SortOrder.NAME_ASC
                    3 -> SortOrder.NAME_DESC
                    else -> SortOrder.SCORE_DESC
                }

                if (currentSortOrder != newSortOrder) {
                    currentSortOrder = newSortOrder
                    Log.d(TAG, "Sort order changed to: $currentSortOrder")
                    applyFilterAndSort()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadData() {
        val currentTask = task ?: return

        loadingView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE

        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 작업이 완료 상태인지 확인
                if (currentTask.status != TaskStatus.COMPLETED) {
                    showError("작업이 아직 완료되지 않았습니다.")
                    return@launch
                }

                // TaskResult 가져오기
                val result = taskRepository.getTaskResult(currentTask.id)
                if (result == null) {
                    showError("결과 데이터를 찾을 수 없습니다.")
                    return@launch
                }

                // Raw data 로드
                val rawData = if (result.rawData != null) {
                    result.rawData
                } else if (result.data?.containsKey("rawDataFile") == true) {
                    val filePath = result.data["rawDataFile"] as? String
                    if (filePath != null) {
                        withContext(Dispatchers.IO) {
                            taskRepository.loadRawDataFromFile(filePath)
                        }
                    } else null
                } else null

                if (rawData == null) {
                    showError("이름 데이터를 찾을 수 없습니다.")
                    return@launch
                }

                // GeneratedName 리스트로 파싱
                val type = object : TypeToken<List<GeneratedName>>() {}.type
                allNames = gson.fromJson(rawData, type)

                Log.d(TAG, "Loaded ${allNames.size} names")

                // UI 업데이트
                withContext(Dispatchers.Main) {
                    loadingView.visibility = View.GONE

                    if (allNames.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        applyFilterAndSort()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading data", e)
                withContext(Dispatchers.Main) {
                    showError("데이터 로드 중 오류가 발생했습니다: ${e.message}")
                }
            }
        }
    }

    private fun applyFilterAndSort() {
        Log.d(TAG, "Applying filter and sort. Query: '$currentSearchQuery', Sort: $currentSortOrder")

        // 1. 필터링 먼저 수행
        val filtered = if (currentSearchQuery.isEmpty()) {
            allNames
        } else {
            allNames.filter { name ->
                searchHelper.matches(name, currentSearchQuery)
            }
        }

        Log.d(TAG, "Filtered ${filtered.size} names from ${allNames.size}")

        // 2. 정렬 수행
        filteredNames = when (currentSortOrder) {
            SortOrder.SCORE_DESC -> {
                filtered.sortedByDescending { it.analysisInfo?.totalScore ?: 0 }
            }
            SortOrder.SCORE_ASC -> {
                filtered.sortedBy { it.analysisInfo?.totalScore ?: 0 }
            }
            SortOrder.NAME_ASC -> {
                filtered.sortedWith { a, b ->
                    val nameA = "${a.surnameHangul}${a.combinedPronounciation}"
                    val nameB = "${b.surnameHangul}${b.combinedPronounciation}"
                    koreanCollator.compare(nameA, nameB)
                }
            }
            SortOrder.NAME_DESC -> {
                filtered.sortedWith { a, b ->
                    val nameA = "${a.surnameHangul}${a.combinedPronounciation}"
                    val nameB = "${b.surnameHangul}${b.combinedPronounciation}"
                    koreanCollator.compare(nameB, nameA)
                }
            }
        }

        // 3. 어댑터 업데이트
        adapter.submitList(filteredNames) {
            recyclerView.post {
                recyclerView.scrollToPosition(0)
            }
        }

        // 4. Empty view 업데이트
        if (filteredNames.isEmpty()) {
            if (currentSearchQuery.isNotEmpty()) {
                emptyView.text = "'$currentSearchQuery'에 대한 검색 결과가 없습니다"
            } else {
                emptyView.text = "이름 데이터가 없습니다"
            }
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }

        Log.d(TAG, "Updated adapter with ${filteredNames.size} names")

        filteredNames.take(5).forEachIndexed { index, name ->
            Log.d(TAG, "[$index] ${name.surnameHangul}${name.combinedPronounciation} - Score: ${name.analysisInfo?.totalScore}")
        }
    }

    private fun showNameDetail(name: GeneratedName) {
        NameDetailDialog(requireContext(), name).show()
    }

    private fun showError(message: String) {
        loadingView.visibility = View.GONE
        emptyView.text = message
        emptyView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun getTaskTypeName(type: TaskType): String {
        return when (type) {
            TaskType.NAMING -> "작명"
            TaskType.EVALUATION -> "평가"
            TaskType.COMPARISON -> "비교"
            TaskType.REPORT_GENERATION -> "보고서"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadJob?.cancel()
        recyclerView.adapter = null
    }

    enum class SortOrder {
        SCORE_DESC, SCORE_ASC, NAME_ASC, NAME_DESC
    }
}