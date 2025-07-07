// ui/history/NameListDialog.kt
package com.ssc.namespring.ui.history

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import java.text.Collator
import java.text.SimpleDateFormat
import java.util.*

class NameListDialog(
    private val activity: AppCompatActivity,
    private val task: Task,
    private val taskRepository: TaskRepository
) : Dialog(activity, android.R.style.Theme_Material_Light_Dialog) {

    companion object {
        private const val TAG = "NameListDialog"
    }

    private lateinit var searchView: SearchView
    private lateinit var sortSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var loadingView: ProgressBar
    private lateinit var btnTaskInfo: MaterialButton

    private lateinit var adapter: NameListAdapter
    private lateinit var favoriteRepository: FavoriteNameRepository

    // CoroutineScope를 Job과 함께 생성
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    // 개별 로드 작업을 위한 Job
    private var loadJob: Job? = null

    private var birthDateTimeMillis: Long = 0L

    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREAN)
    private val koreanCollator = Collator.getInstance(Locale.KOREAN)  // 한글 정렬용

    private var allNames: List<GeneratedName> = emptyList()
    private var filteredNames: List<GeneratedName> = emptyList()
    private var birthDateTime: String = ""
    private var currentSearchQuery = ""
    private var currentSortOrder = SortOrder.SCORE_DESC

    private val searchHelper = NameSearchHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_name_list)

        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        setupViews()
        loadData()
    }

    private fun setupViews() {
        // View 초기화
        searchView = findViewById(R.id.searchView)
        sortSpinner = findViewById(R.id.sortSpinner)
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)
        loadingView = findViewById(R.id.loadingView)
        btnTaskInfo = findViewById(R.id.btnTaskInfo)

        // 타이틀 설정
        findViewById<TextView>(R.id.tvTitle).text =
            "${task.inputData["profileName"] ?: "작업"} - ${getTaskTypeName(task.type)} 결과"

        // 생년월일시 정보 추출
        task.inputData["birthDateTime"]?.let { birthDateTimeStr ->
            val millis = (birthDateTimeStr as? String)?.toLongOrNull()
            millis?.let {
                birthDateTime = dateFormat.format(Date(it))
            }
        }

        // FavoriteRepository 초기화
        favoriteRepository = FavoriteNameRepository.getInstance(context)

        // 생년월일시 정보 추출
        task.inputData["birthDateTime"]?.let { birthDateTimeStr ->
            val millis = (birthDateTimeStr as? String)?.toLongOrNull()
            millis?.let {
                birthDateTimeMillis = it
                birthDateTime = dateFormat.format(Date(it))
            }
        }

        // RecyclerView 설정
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = NameListAdapter(
            birthDateTime = birthDateTime,
            birthDateTimeMillis = birthDateTimeMillis,
            onNameClick = { name ->
                showNameDetail(name)
            },
            favoriteRepository = favoriteRepository,
            lifecycleOwner = activity  // activity를 LifecycleOwner로 전달
        )
        recyclerView.adapter = adapter
        // 검색 설정
        setupSearch()

        // 정렬 설정
        setupSort()

        // 버튼 설정
        btnTaskInfo.setOnClickListener {
            TaskDetailDialog(context, task, taskRepository).show()
        }

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            cleanupAndDismiss()
        }
    }

    private fun setupSearch() {
        // SearchView 텍스트 색상 설정
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText?.apply {
            setTextColor(context.getColor(R.color.text_primary))
            setHintTextColor(context.getColor(R.color.text_secondary))
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
            context,
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
        loadingView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE

        // loadJob에 할당
        loadJob = scope.launch {
            try {
                // 작업이 완료 상태인지 확인
                if (task.status != TaskStatus.COMPLETED) {
                    showError("작업이 아직 완료되지 않았습니다.")
                    return@launch
                }

                // TaskResult 가져오기
                val result = taskRepository.getTaskResult(task.id)
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
                // 한글 정렬을 위해 Collator 사용
                filtered.sortedWith { a, b ->
                    koreanCollator.compare(a.combinedPronounciation, b.combinedPronounciation)
                }
            }
            SortOrder.NAME_DESC -> {
                // 한글 역순 정렬
                filtered.sortedWith { a, b ->
                    koreanCollator.compare(b.combinedPronounciation, a.combinedPronounciation)
                }
            }
        }

        // 3. 어댑터 업데이트
        adapter.submitList(filteredNames) {
            // 즉시 맨 위로 이동
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

        // 디버그용: 처음 5개 이름 출력
        filteredNames.take(5).forEachIndexed { index, name ->
            Log.d(TAG, "[$index] ${name.combinedPronounciation} - Score: ${name.analysisInfo?.totalScore}")
        }
    }

    private fun showNameDetail(name: GeneratedName) {
        NameDetailDialog(context, name).show()
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

    private fun cleanupAndDismiss() {
        // 1. 진행 중인 작업 취소
        loadJob?.cancel()

        // 2. 전체 scope 취소
        job.cancel()

        // 3. RecyclerView 정리
        recyclerView.adapter = null

        // 4. Dialog 닫기
        dismiss()
    }

    override fun onBackPressed() {
        cleanupAndDismiss()
    }

    override fun dismiss() {
        // 정리 작업
        loadJob?.cancel()
        job.cancel()
        super.dismiss()
    }

    enum class SortOrder {
        SCORE_DESC, SCORE_ASC, NAME_ASC, NAME_DESC
    }
}