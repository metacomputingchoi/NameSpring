// ui/history/managers/NameListSortManager.kt
package com.ssc.namespring.ui.history.managers

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class NameListSortManager(
    private val onSortOrderChanged: (SortOrder) -> Unit
) {
    companion object {
        private const val TAG = "NameListSortManager"
    }

    private var currentSortOrder = SortOrder.SCORE_DESC

    fun setupSortSpinner(spinner: Spinner) {
        val sortOptions = arrayOf(
            "점수 높은순",
            "점수 낮은순", 
            "이름순 (가→하)",
            "이름순 (하→가)"
        )

        spinner.adapter = ArrayAdapter(
            spinner.context,
            android.R.layout.simple_spinner_dropdown_item,
            sortOptions
        )

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, 
                view: View?, 
                position: Int, 
                id: Long
            ) {
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
                    onSortOrderChanged(newSortOrder)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    enum class SortOrder {
        SCORE_DESC, SCORE_ASC, NAME_ASC, NAME_DESC
    }
}