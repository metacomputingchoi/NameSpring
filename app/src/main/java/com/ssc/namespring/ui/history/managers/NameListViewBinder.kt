// ui/history/managers/NameListViewBinder.kt
package com.ssc.namespring.ui.history.managers

import android.app.Dialog
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.ui.history.adapter.NameListAdapter
import com.ssc.namingengine.data.GeneratedName
import java.text.SimpleDateFormat
import java.util.*

class NameListViewBinder(
    private val dialog: Dialog,
    private val activity: AppCompatActivity,
    private val task: Task
) {
    private lateinit var searchView: SearchView
    private lateinit var sortSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var loadingView: ProgressBar
    private lateinit var btnTaskInfo: MaterialButton
    private lateinit var adapter: NameListAdapter

    private val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREAN)
    private var birthDateTimeMillis: Long = 0L
    private var birthDateTime: String = ""

    var onTaskInfoClick: (() -> Unit)? = null
    var onCloseClick: (() -> Unit)? = null
    var onNameClick: ((GeneratedName) -> Unit)? = null

    fun setManagers(searchManager: NameListSearchManager, sortManager: NameListSortManager) {
        searchManager.setupSearchView(searchView)
        sortManager.setupSortSpinner(sortSpinner)
    }

    fun setupViews() {
        with(dialog) {
            searchView = findViewById(R.id.searchView)
            sortSpinner = findViewById(R.id.sortSpinner)
            recyclerView = findViewById(R.id.recyclerView)
            emptyView = findViewById(R.id.emptyView)
            loadingView = findViewById(R.id.loadingView)
            btnTaskInfo = findViewById(R.id.btnTaskInfo)

            findViewById<TextView>(R.id.tvTitle).text =
                "${task.inputData["profileName"] ?: "작업"} - ${getTaskTypeName(task.type)} 결과"

            task.inputData["birthDateTime"]?.let { birthDateTimeStr ->
                (birthDateTimeStr as? String)?.toLongOrNull()?.let {
                    birthDateTimeMillis = it
                    birthDateTime = dateFormat.format(Date(it))
                }
            }

            setupRecyclerView()
            setupButtons()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(dialog.context)
        adapter = NameListAdapter(
            birthDateTime = birthDateTime,
            birthDateTimeMillis = birthDateTimeMillis,
            onNameClick = { name -> onNameClick?.invoke(name) },
            favoriteRepository = FavoriteNameRepository.getInstance(dialog.context),
            lifecycleOwner = activity
        )
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        btnTaskInfo.setOnClickListener { onTaskInfoClick?.invoke() }
        dialog.findViewById<ImageButton>(R.id.btnClose).setOnClickListener { 
            onCloseClick?.invoke() 
        }
    }

    fun showLoading() {
        loadingView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE
    }

    fun showError(message: String) {
        loadingView.visibility = View.GONE
        emptyView.text = message
        emptyView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    fun updateList(names: List<GeneratedName>, searchQuery: String) {
        loadingView.visibility = View.GONE

        if (names.isEmpty()) {
            emptyView.text = if (searchQuery.isNotEmpty()) {
                "'$searchQuery'에 대한 검색 결과가 없습니다"
            } else {
                "이름 데이터가 없습니다"
            }
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.submitList(names) {
                recyclerView.post { recyclerView.scrollToPosition(0) }
            }
        }
    }

    fun cleanup() {
        recyclerView.adapter = null
    }

    private fun getTaskTypeName(type: TaskType) = when (type) {
        TaskType.NAMING -> "작명"
        TaskType.EVALUATION -> "평가"
        TaskType.COMPARISON -> "비교"
        TaskType.REPORT_GENERATION -> "보고서"
    }
}