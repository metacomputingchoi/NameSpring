package com.ssc.namespring

import java.util.UUID
import android.app.ProgressDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.ssc.namespring.model.domain.entity.*
import com.ssc.namespring.model.domain.service.workmanager.TaskWorkManager
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.model.presentation.adapter.TaskHistoryAdapter
import com.ssc.namespring.ui.history.NameListDialog
import com.ssc.namespring.ui.history.NameListDialogFragment
import com.ssc.namespring.ui.history.TaskDetailDialog
import com.ssc.namespring.utils.search.TaskSearchHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity(), TaskHistoryAdapter.SelectionListener {

    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var chipGroup: ChipGroup
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var searchView: SearchView
    private lateinit var sortSpinner: Spinner
    private lateinit var fabScrollTop: FloatingActionButton

    // 선택 모드 UI
    private lateinit var selectionModeButtons: LinearLayout
    private lateinit var btnSelectAll: Button
    private lateinit var btnClearSelection: Button
    private lateinit var btnDeleteSelected: Button

    private lateinit var taskWorkManager: TaskWorkManager
    private lateinit var taskRepository: TaskRepository
    private lateinit var adapter: TaskHistoryAdapter
    private lateinit var searchHelper: TaskSearchHelper

    private var currentProfileId: String? = null
    private var currentFilter: TaskFilter = TaskFilter.ALL
    private var currentSortOrder: SortOrder = SortOrder.DATE_DESC
    private var currentSearchQuery: String = ""

    // 전체 데이터와 필터링된 데이터 분리
    private var allTasks: List<Task> = emptyList()
    private var filteredTasks: List<Task> = emptyList()

    // 페이지네이션
    private var currentPage = 0
    private val pageSize = 50
    private var isLoadingMore = false
    private var hasMoreData = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        currentProfileId = intent.getStringExtra("profile_id")

        initializeComponents()
        setupViews()
        observeData()
    }

    private fun initializeComponents() {
        taskWorkManager = TaskWorkManager.getInstance(this)
        taskRepository = TaskRepository.getInstance(this)
        searchHelper = TaskSearchHelper()

        adapter = TaskHistoryAdapter(
            onTaskClick = { task ->
                if (adapter.isSelectionMode) {
                    adapter.toggleSelection(task.id)
                } else {
                    showTaskDetail(task)
                }
            },
            onTaskLongClick = { task ->
                if (!adapter.isSelectionMode) {
                    enterSelectionMode()
                    adapter.toggleSelection(task.id)
                }
                true
            },
            onTaskCancel = { task ->
                cancelTask(task)
            },
            onTaskRetry = { task ->
                retryTask(task)
            },
            selectionListener = this
        )
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        chipGroup = findViewById(R.id.chipGroup)
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)
        searchView = findViewById(R.id.searchView)
        sortSpinner = findViewById(R.id.sortSpinner)
        fabScrollTop = findViewById(R.id.fabScrollTop)

        // 선택 모드 버튼들
        selectionModeButtons = findViewById(R.id.selectionModeButtons)
        btnSelectAll = findViewById(R.id.btnSelectAll)
        btnClearSelection = findViewById(R.id.btnClearSelection)
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "작업 기록"

        // RecyclerView 설정
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        // 스크롤 리스너 (페이지네이션)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // FAB 표시/숨김
                if (dy > 0 && fabScrollTop.visibility == View.VISIBLE) {
                    fabScrollTop.hide()
                } else if (dy < 0 && fabScrollTop.visibility != View.VISIBLE) {
                    fabScrollTop.show()
                }

                // 페이지네이션
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (!isLoadingMore && hasMoreData && lastVisibleItem >= totalItemCount - 5) {
                    loadMoreItems()
                }
            }
        })

        setupTabs()
        setupFilters()
        setupSearch()
        setupSort()
        setupFab()
        setupSelectionModeButtons()
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("전체"))
        tabLayout.addTab(tabLayout.newTab().setText("완료"))
        tabLayout.addTab(tabLayout.newTab().setText("활성/대기"))  // 변경
        tabLayout.addTab(tabLayout.newTab().setText("취소"))
        tabLayout.addTab(tabLayout.newTab().setText("실패"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentFilter = when (tab.position) {
                    1 -> TaskFilter.COMPLETED
                    2 -> TaskFilter.ACTIVE_QUEUE  // 변경
                    3 -> TaskFilter.CANCELLED
                    4 -> TaskFilter.FAILED
                    else -> TaskFilter.ALL
                }
                resetPagination()
                applyFiltersAndSort()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    // 작업 취소 메서드 추가
    private fun cancelTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("작업 취소")
            .setMessage("이 작업을 취소하시겠습니까?")
            .setPositiveButton("취소") { _, _ ->
                lifecycleScope.launch {
                    try {
                        // WorkManager를 통해 작업 취소
                        taskWorkManager.cancelTask(task.id)

                        // 상태 업데이트를 위해 잠시 대기
                        withContext(Dispatchers.IO) {
                            kotlinx.coroutines.delay(500)
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@HistoryActivity,
                                "작업이 취소되었습니다",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@HistoryActivity,
                                "작업 취소 중 오류가 발생했습니다",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("아니오", null)
            .show()
    }

    // 작업 재시도 메서드 추가
    private fun retryTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("작업 재시도")
            .setMessage("이 작업을 다시 시도하시겠습니까?")
            .setPositiveButton("재시도") { _, _ ->
                // 새로운 Task 생성 (ID는 새로 생성)
                val newTask = task.copy(
                    id = UUID.randomUUID().toString(),
                    status = TaskStatus.PENDING,
                    progress = 0,
                    errorMessage = null,
                    createdAt = System.currentTimeMillis(),
                    startedAt = null,
                    completedAt = null,
                    workerId = null
                )

                // WorkManager에 새 작업 추가
                taskWorkManager.enqueueTask(newTask)

                Toast.makeText(
                    this,
                    "작업을 다시 시작했습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun setupFilters() {
        TaskType.values().forEach { type ->
            val chip = Chip(this).apply {
                text = getTaskTypeName(type)
                isCheckable = true
                setOnCheckedChangeListener { _, _ ->
                    resetPagination()
                    applyFiltersAndSort()
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun setupSearch() {
        // SearchView 텍스트 색상 설정
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText?.apply {
            setTextColor(getColor(R.color.text_primary))
            setHintTextColor(getColor(R.color.text_secondary))
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                resetPagination()
                applyFiltersAndSort()
                return true
            }
        })
    }

    private fun setupSort() {
        val sortOptions = arrayOf(
            "최신순", "오래된순",
            "작업명순", "작업명역순",
            "유형순", "상태순"
        )

        sortSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sortOptions)
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSortOrder = SortOrder.values()[position]
                resetPagination()
                applyFiltersAndSort()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFab() {
        fabScrollTop.setOnClickListener {
            recyclerView.smoothScrollToPosition(0)
        }
    }

    private fun setupSelectionModeButtons() {
        btnSelectAll.setOnClickListener {
            val visibleSelectedCount = adapter.getVisibleSelectedCount()
            val visibleTotalCount = adapter.currentList.size

            if (visibleSelectedCount == visibleTotalCount) {
                // 현재 보이는 항목이 모두 선택된 상태면 선택 해제
                adapter.deselectVisible()
                btnSelectAll.text = "전체 선택"
            } else {
                // 그렇지 않으면 전체 선택
                adapter.selectVisible()
                btnSelectAll.text = "선택 해제"
            }
        }

        btnClearSelection.setOnClickListener {
            adapter.clearSelection()
        }

        btnDeleteSelected.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun observeData() {
        if (currentProfileId != null) {
            taskRepository.taskHistoryMap.observe(this, Observer { historyMap ->
                val history = historyMap[currentProfileId]
                allTasks = history?.tasks ?: emptyList()
                resetPagination()
                applyFiltersAndSort()
            })
        } else {
            taskRepository.taskHistoryMap.observe(this, Observer { historyMap ->
                allTasks = historyMap.values.flatMap { it.tasks }
                resetPagination()
                applyFiltersAndSort()
            })
        }
    }

    private fun resetPagination() {
        currentPage = 0
        hasMoreData = true
        isLoadingMore = false
    }

    private fun applyFiltersAndSort() {
        // 필터링
        filteredTasks = allTasks.filter { task ->
            val statusMatch = when (currentFilter) {
                TaskFilter.ALL -> true
                TaskFilter.ACTIVE_QUEUE -> task.status in listOf(TaskStatus.RUNNING, TaskStatus.PENDING)  // 변경
                TaskFilter.COMPLETED -> task.status == TaskStatus.COMPLETED
                TaskFilter.CANCELLED -> task.status == TaskStatus.CANCELLED
                TaskFilter.FAILED -> task.status == TaskStatus.FAILED
            }

            val typeMatch = if (chipGroup.checkedChipIds.isEmpty()) {
                true
            } else {
                chipGroup.checkedChipIds.any { chipId ->
                    val chip = findViewById<Chip>(chipId)
                    chip.text == getTaskTypeName(task.type)
                }
            }

            val searchMatch = if (currentSearchQuery.isEmpty()) {
                true
            } else {
                searchHelper.matches(task, currentSearchQuery)
            }

            statusMatch && typeMatch && searchMatch
        }

        // 정렬 적용 - 활성/대기 탭에서는 특별한 정렬 적용
        filteredTasks = if (currentFilter == TaskFilter.ACTIVE_QUEUE) {
            // 작업 큐에서는 진행중인 작업을 먼저, 그 다음 대기중인 작업을 시간순으로
            filteredTasks.sortedWith(compareBy(
                { it.status != TaskStatus.RUNNING },  // RUNNING을 먼저
                { it.status != TaskStatus.PENDING },   // 그 다음 PENDING
                { it.createdAt }                       // 생성 시간순
            ))
        } else {
            when (currentSortOrder) {
                SortOrder.DATE_DESC -> filteredTasks.sortedByDescending { it.createdAt }
                SortOrder.DATE_ASC -> filteredTasks.sortedBy { it.createdAt }
                SortOrder.NAME_ASC -> filteredTasks.sortedBy { getTaskDisplayName(it) }
                SortOrder.NAME_DESC -> filteredTasks.sortedByDescending { getTaskDisplayName(it) }
                SortOrder.TYPE -> filteredTasks.sortedBy { it.type.name }
                SortOrder.STATUS -> filteredTasks.sortedBy { it.status.name }
            }
        }

        // 페이지네이션 적용
        loadInitialItems()

        // 선택 상태 업데이트
        updateSelectionUI()
    }

    private fun loadInitialItems() {
        val endIndex = minOf(pageSize, filteredTasks.size)
        val initialItems = filteredTasks.subList(0, endIndex)
        adapter.submitList(initialItems)

        hasMoreData = endIndex < filteredTasks.size
        currentPage = 1

        emptyView.visibility = if (initialItems.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (initialItems.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun loadMoreItems() {
        if (isLoadingMore || !hasMoreData) return

        isLoadingMore = true
        val startIndex = currentPage * pageSize
        val endIndex = minOf(startIndex + pageSize, filteredTasks.size)

        if (startIndex < filteredTasks.size) {
            val currentItems = adapter.currentList.toMutableList()
            val newItems = filteredTasks.subList(startIndex, endIndex)
            currentItems.addAll(newItems)

            adapter.submitList(currentItems) {
                isLoadingMore = false
                currentPage++
                hasMoreData = endIndex < filteredTasks.size
            }
        } else {
            isLoadingMore = false
            hasMoreData = false
        }
    }

    private fun getTaskDisplayName(task: Task): String {
        return task.inputData["profileName"] as? String ?: "작업 ${task.id.take(8)}"
    }

    private fun showTaskDetail(task: Task) {
        when (task.type) {
            TaskType.NAMING, TaskType.EVALUATION -> {
                // Task ID만 전달
                NameListDialogFragment.newInstance(task.id)
                    .show(supportFragmentManager, "NameListDialog")
            }
            else -> {
                TaskDetailDialog(this, task, taskRepository).show()
            }
        }
    }

    private fun enterSelectionMode() {
        adapter.isSelectionMode = true
        selectionModeButtons.visibility = View.VISIBLE
        // FAB 위치 조정
        (fabScrollTop.layoutParams as CoordinatorLayout.LayoutParams).apply {
            bottomMargin = resources.getDimensionPixelSize(R.dimen.fab_margin_with_selection)
        }
        supportActionBar?.title = "항목 선택"
        invalidateOptionsMenu()
    }

    private fun exitSelectionMode() {
        adapter.clearSelection()
        adapter.isSelectionMode = false
        selectionModeButtons.visibility = View.GONE
        // FAB 위치 원복
        (fabScrollTop.layoutParams as CoordinatorLayout.LayoutParams).apply {
            bottomMargin = resources.getDimensionPixelSize(R.dimen.fab_margin_normal)
        }
        supportActionBar?.title = "작업 기록"
        invalidateOptionsMenu()
    }

    override fun onSelectionChanged(selectedCount: Int) {
        updateSelectionUI()
    }

    private fun updateSelectionUI() {
        val selectedCount = adapter.getSelectedCount()
        val visibleSelectedCount = adapter.getVisibleSelectedCount()
        val visibleTotalCount = adapter.currentList.size

        if (adapter.isSelectionMode) {
            supportActionBar?.title = "$selectedCount 개 선택됨"

            // 선택된 항목이 없으면 삭제 버튼 비활성화
            btnDeleteSelected.isEnabled = selectedCount > 0

            // 전체 선택/해제 버튼 텍스트 업데이트
            btnSelectAll.text = if (visibleSelectedCount == visibleTotalCount && visibleTotalCount > 0) {
                "선택 해제"
            } else {
                "전체 선택"
            }
        }
    }

    private fun showDeleteConfirmation() {
        val selectedIds = adapter.getSelectedTaskIds()
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "선택된 항목이 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        // 선택된 작업들 가져오기
        val selectedTasks = allTasks.filter { it.id in selectedIds }

        val activeCount = selectedTasks.count { it.status in listOf(TaskStatus.RUNNING, TaskStatus.PENDING) }  // 변경
        val message = if (activeCount > 0) {
            "${selectedIds.size}개의 작업을 삭제하시겠습니까?\n(진행/대기 중인 작업 ${activeCount}개가 중단됩니다)"
        } else {
            "${selectedIds.size}개의 작업을 삭제하시겠습니까?"
        }

        AlertDialog.Builder(this)
            .setTitle("작업 삭제")
            .setMessage(message)
            .setPositiveButton("삭제") { _, _ ->
                deleteSelectedTasks(selectedTasks)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deleteSelectedTasks(tasks: List<Task>) {
        lifecycleScope.launch {
            // 프로그레스 다이얼로그 표시
            val progressDialog = ProgressDialog(this@HistoryActivity).apply {
                setMessage("삭제 중...")
                setCancelable(false)
                show()
            }

            try {
                tasks.forEach { task ->
                    when (task.status) {
                        TaskStatus.PENDING, TaskStatus.RUNNING -> {
                            // 진행 중인 작업은 취소 후 삭제
                            taskWorkManager.cancelTask(task.id)
                            withContext(Dispatchers.IO) {
                                // 잠시 대기 후 삭제
                                kotlinx.coroutines.delay(100)
                                taskRepository.deleteTask(task.id)
                            }
                        }
                        else -> {
                            // 완료/실패 작업은 바로 삭제
                            withContext(Dispatchers.IO) {
                                taskRepository.deleteTask(task.id)
                            }
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    exitSelectionMode()
                    Toast.makeText(
                        this@HistoryActivity,
                        "${tasks.size}개의 작업이 삭제되었습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@HistoryActivity,
                        "삭제 중 오류가 발생했습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)

        // 선택 모드일 때 선택 버튼 숨기기
        menu.findItem(R.id.action_select_mode)?.isVisible = !adapter.isSelectionMode

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_select_mode -> {
                enterSelectionMode()
                true
            }
            R.id.action_load_all -> {
                loadAllItems()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadAllItems() {
        if (filteredTasks.size > adapter.currentList.size) {
            AlertDialog.Builder(this)
                .setTitle("전체 로드")
                .setMessage("${filteredTasks.size}개의 항목을 모두 로드하시겠습니까?")
                .setPositiveButton("로드") { _, _ ->
                    adapter.submitList(filteredTasks.toList())
                    hasMoreData = false
                    Toast.makeText(this, "전체 항목을 로드했습니다", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("취소", null)
                .show()
        } else {
            Toast.makeText(this, "이미 모든 항목이 로드되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (adapter.isSelectionMode) {
            exitSelectionMode()
        } else {
            super.onBackPressed()
        }
    }

    private fun getTaskTypeName(type: TaskType): String {
        return when (type) {
            TaskType.NAMING -> "작명"
            TaskType.EVALUATION -> "평가"
            TaskType.COMPARISON -> "비교"
            TaskType.REPORT_GENERATION -> "보고서"
        }
    }

    enum class TaskFilter {
        ALL, COMPLETED, ACTIVE_QUEUE, CANCELLED, FAILED
    }

    enum class SortOrder {
        DATE_DESC, DATE_ASC, NAME_ASC, NAME_DESC, TYPE, STATUS
    }
}