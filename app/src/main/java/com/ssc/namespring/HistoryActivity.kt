// HistoryActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import com.ssc.namespring.model.domain.entity.*
import com.ssc.namespring.model.domain.service.workmanager.TaskWorkManager
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namespring.model.presentation.adapter.TaskHistoryAdapter
import com.ssc.namespring.ui.history.TaskDetailDialog

class HistoryActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var chipGroup: ChipGroup
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    private lateinit var taskWorkManager: TaskWorkManager
    private lateinit var taskRepository: TaskRepository
    private lateinit var adapter: TaskHistoryAdapter

    private var currentProfileId: String? = null
    private var currentFilter: TaskFilter = TaskFilter.ALL

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
        adapter = TaskHistoryAdapter(
            onTaskClick = { task -> showTaskDetail(task) },
            onTaskCancel = { task -> cancelTask(task) },
            onTaskRetry = { task -> retryTask(task) }
        )
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        chipGroup = findViewById(R.id.chipGroup)
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "작업 기록"

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupTabs()
        setupFilters()
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("전체"))
        tabLayout.addTab(tabLayout.newTab().setText("활성"))
        tabLayout.addTab(tabLayout.newTab().setText("완료"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentFilter = when (tab.position) {
                    1 -> TaskFilter.ACTIVE
                    2 -> TaskFilter.COMPLETED
                    else -> TaskFilter.ALL
                }
                updateTaskList()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupFilters() {
        TaskType.values().forEach { type ->
            val chip = Chip(this).apply {
                text = getTaskTypeName(type)
                isCheckable = true
                setOnCheckedChangeListener { _, _ -> updateTaskList() }
            }
            chipGroup.addView(chip)
        }
    }

    private fun observeData() {
        if (currentProfileId != null) {
            taskRepository.taskHistoryMap.observe(this, Observer { historyMap ->
                val history = historyMap[currentProfileId]
                updateTaskList(history?.tasks ?: emptyList())
            })
        } else {
            // Show all tasks from all profiles
            taskRepository.taskHistoryMap.observe(this, Observer { historyMap ->
                val allTasks = historyMap.values.flatMap { it.tasks }
                updateTaskList(allTasks)
            })
        }
    }

    private fun updateTaskList(allTasks: List<Task>? = null) {
        val tasks = allTasks ?: taskRepository.getTaskHistory(currentProfileId ?: "")

        val filteredTasks = tasks.filter { task ->
            val statusMatch = when (currentFilter) {
                TaskFilter.ALL -> true
                TaskFilter.ACTIVE -> task.isActive()
                TaskFilter.COMPLETED -> task.status == TaskStatus.COMPLETED
            }

            val typeMatch = if (chipGroup.checkedChipIds.isEmpty()) {
                true
            } else {
                chipGroup.checkedChipIds.any { chipId ->
                    val chip = findViewById<Chip>(chipId)
                    chip.text == getTaskTypeName(task.type)
                }
            }

            statusMatch && typeMatch
        }.sortedByDescending { it.createdAt }

        adapter.submitList(filteredTasks)

        emptyView.visibility = if (filteredTasks.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (filteredTasks.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showTaskDetail(task: Task) {
        TaskDetailDialog(this, task, taskRepository).show()
    }

    private fun cancelTask(task: Task) {
        taskWorkManager.cancelTask(task.id)
    }

    private fun retryTask(task: Task) {
        val newTask = task.copy(
            id = java.util.UUID.randomUUID().toString(),
            status = TaskStatus.PENDING,
            progress = 0,
            errorMessage = null,
            createdAt = System.currentTimeMillis(),
            startedAt = null,
            completedAt = null,
            workerId = null
        )
        taskWorkManager.enqueueTask(newTask)
    }

    private fun getTaskTypeName(type: TaskType): String {
        return when (type) {
            TaskType.NAMING -> "작명"
            TaskType.EVALUATION -> "평가"
            TaskType.COMPARISON -> "비교"
            TaskType.REPORT_GENERATION -> "보고서"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    enum class TaskFilter {
        ALL, ACTIVE, COMPLETED
    }
}
