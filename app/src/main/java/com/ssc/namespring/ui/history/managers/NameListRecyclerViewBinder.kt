// ui/history/managers/NameListRecyclerViewBinder.kt
package com.ssc.namespring.ui.history.managers

import android.app.Dialog
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.ui.history.adapter.NameListAdapter
import com.ssc.namingengine.data.GeneratedName

class NameListRecyclerViewBinder(
    private val dialog: Dialog,
    private val activity: AppCompatActivity,
    private val task: Task
) {
    private val viewHolder = NameListViewHolder(dialog)
    private val dateFormatter = NameListDateFormatter()
    private lateinit var adapter: NameListAdapter
    private val dateInfo = dateFormatter.extractDateInfo(task)

    var onTaskInfoClick: (() -> Unit)? = null
    var onCloseClick: (() -> Unit)? = null
    var onNameClick: ((GeneratedName) -> Unit)? = null

    fun setManagers(searchManager: NameListSearchManager, sortManager: NameListSortManager) {
        searchManager.setupSearchView(viewHolder.searchView)
        sortManager.setupSortSpinner(viewHolder.sortSpinner)
    }

    fun setupViews() {
        viewHolder.tvTitle.text = buildTitleText()
        setupRecyclerView()
        setupButtons()
    }

    private fun buildTitleText(): String {
        val profileName = task.inputData["profileName"] ?: "작업"
        val taskTypeName = getTaskTypeName(task.type)
        return "$profileName - $taskTypeName 결과"
    }

    private fun setupRecyclerView() {
        viewHolder.recyclerView.layoutManager = LinearLayoutManager(dialog.context)
        adapter = NameListAdapter(
            birthDateTime = dateInfo.formatted,
            birthDateTimeMillis = dateInfo.millis,
            onNameClick = { name -> onNameClick?.invoke(name) },
            favoriteRepository = FavoriteNameRepository.getInstance(dialog.context),
            lifecycleOwner = activity
        )
        viewHolder.recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        viewHolder.btnTaskInfo.setOnClickListener { onTaskInfoClick?.invoke() }
        viewHolder.btnClose.setOnClickListener { onCloseClick?.invoke() }
    }

    fun showLoading() {
        updateVisibility(loading = true, empty = false, recycler = false)
    }

    fun showError(message: String) {
        viewHolder.emptyView.text = message
        updateVisibility(loading = false, empty = true, recycler = false)
    }

    fun updateList(names: List<GeneratedName>, searchQuery: String) {
        if (names.isEmpty()) {
            showEmptyState(searchQuery)
        } else {
            showListState(names)
        }
    }

    private fun showEmptyState(searchQuery: String) {
        viewHolder.emptyView.text = if (searchQuery.isNotEmpty()) {
            "'$searchQuery'에 대한 검색 결과가 없습니다"
        } else {
            "이름 데이터가 없습니다"
        }
        updateVisibility(loading = false, empty = true, recycler = false)
    }

    private fun showListState(names: List<GeneratedName>) {
        updateVisibility(loading = false, empty = false, recycler = true)
        adapter.submitList(names) {
            viewHolder.recyclerView.post { viewHolder.recyclerView.scrollToPosition(0) }
        }
    }

    private fun updateVisibility(loading: Boolean, empty: Boolean, recycler: Boolean) {
        viewHolder.loadingView.visibility = if (loading) View.VISIBLE else View.GONE
        viewHolder.emptyView.visibility = if (empty) View.VISIBLE else View.GONE
        viewHolder.recyclerView.visibility = if (recycler) View.VISIBLE else View.GONE
    }

    fun cleanup() {
        viewHolder.recyclerView.adapter = null
    }

    private fun getTaskTypeName(type: TaskType) = when (type) {
        TaskType.NAMING -> "작명"
        TaskType.EVALUATION -> "평가"
        TaskType.COMPARISON -> "비교"
        TaskType.REPORT_GENERATION -> "보고서"
    }
}