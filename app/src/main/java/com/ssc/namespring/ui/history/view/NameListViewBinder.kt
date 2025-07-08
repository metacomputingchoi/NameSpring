// ui/history/view/NameListViewBinder.kt
package com.ssc.namespring.ui.history.view

import android.view.View
import android.widget.*
import androidx.appcompat.widget.SearchView
import com.google.android.material.button.MaterialButton
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.ui.history.managers.NameSortManager

class NameListViewBinder(private val view: View) {

    val searchView: SearchView = view.findViewById(R.id.searchView)
    val sortSpinner: Spinner = view.findViewById(R.id.sortSpinner)
    val recyclerView: androidx.recyclerview.widget.RecyclerView = view.findViewById(R.id.recyclerView)
    val emptyView: TextView = view.findViewById(R.id.emptyView)
    val loadingView: ProgressBar = view.findViewById(R.id.loadingView)
    val btnTaskInfo: MaterialButton = view.findViewById(R.id.btnTaskInfo)
    val btnClose: ImageButton = view.findViewById(R.id.btnClose)

    private val tvTitle: TextView = view.findViewById(R.id.tvTitle)

    fun setupTitle(task: Task) {
        tvTitle.text = "${task.inputData["profileName"] ?: "작업"} - ${getTaskTypeName(task.type)} 결과"
    }

    fun setupSearchView(listener: SearchView.OnQueryTextListener) {
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText?.apply {
            setTextColor(view.context.getColor(R.color.text_primary))
            setHintTextColor(view.context.getColor(R.color.text_secondary))
        }
        searchView.setOnQueryTextListener(listener)
    }

    fun setupSortSpinner(listener: (NameSortManager.NameSortOrder) -> Unit) {
        val sortOptions = arrayOf(
            "점수 높은순",
            "점수 낮은순", 
            "이름순 (가→하)",
            "이름순 (하→가)"
        )

        sortSpinner.adapter = ArrayAdapter(
            view.context,
            android.R.layout.simple_spinner_dropdown_item,
            sortOptions
        )

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val nameSortOrder = when (position) {
                    0 -> NameSortManager.NameSortOrder.SCORE_DESC
                    1 -> NameSortManager.NameSortOrder.SCORE_ASC
                    2 -> NameSortManager.NameSortOrder.NAME_ASC
                    3 -> NameSortManager.NameSortOrder.NAME_DESC
                    else -> NameSortManager.NameSortOrder.SCORE_DESC
                }
                listener(nameSortOrder)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun showLoading() {
        loadingView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE
    }

    fun showContent() {
        loadingView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
    }

    fun showEmpty(message: String) {
        loadingView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        emptyView.text = message
    }

    private fun getTaskTypeName(type: TaskType): String {
        return when (type) {
            TaskType.NAMING -> "작명"
            TaskType.EVALUATION -> "평가"
            TaskType.COMPARISON -> "비교"
            TaskType.REPORT_GENERATION -> "보고서"
        }
    }
}