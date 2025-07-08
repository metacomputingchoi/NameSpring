// ui/history/components/namelist/NameListDialogViewManager.kt
package com.ssc.namespring.ui.history.components.namelist

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.ui.history.view.NameListViewBinder
import com.ssc.namespring.ui.history.managers.NameSortManager

class NameListDialogViewManager(
    private val rootView: View,
    private val onSearchQueryChanged: (String) -> Unit,
    private val onSortOrderChanged: (NameSortManager.NameSortOrder) -> Unit,
    private val onTaskInfoClicked: () -> Unit,
    private val onCloseClicked: () -> Unit
) {
    val viewBinder = NameListViewBinder(rootView)
    val recyclerView: RecyclerView get() = viewBinder.recyclerView

    fun setupViews() {
        // RecyclerView 설정
        viewBinder.recyclerView.layoutManager = LinearLayoutManager(rootView.context)

        // 검색 설정
        viewBinder.setupSearchView(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                onSearchQueryChanged(newText ?: "")
                return true
            }
        })

        // 정렬 설정
        viewBinder.setupSortSpinner { sortOrder ->
            onSortOrderChanged(sortOrder)
        }

        // 버튼 설정
        viewBinder.btnTaskInfo.setOnClickListener {
            onTaskInfoClicked()
        }

        viewBinder.btnClose.setOnClickListener {
            onCloseClicked()
        }
    }

    fun showLoading() {
        viewBinder.showLoading()
    }

    fun showEmpty(message: String) {
        viewBinder.showEmpty(message)
    }

    fun showContent() {
        viewBinder.showContent()
    }

    fun setupTitle(task: Task) {
        viewBinder.setupTitle(task)
    }

    fun setDialogSize(window: android.view.Window?) {
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    fun cleanup() {
        viewBinder.recyclerView.adapter = null
    }
}