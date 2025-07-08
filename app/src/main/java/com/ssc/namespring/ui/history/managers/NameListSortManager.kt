// ui/history/managers/NameListSortManager.kt
package com.ssc.namespring.ui.history.managers

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class NameListSortManager(
    private val onSortOrderChanged: (NameListSortOrder) -> Unit
) {
    companion object {
        private const val TAG = "NameListSortManager"
    }

    private var currentNameListSortOrder = NameListSortOrder.SCORE_DESC

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
                val newNameListSortOrder = when (position) {
                    0 -> NameListSortOrder.SCORE_DESC
                    1 -> NameListSortOrder.SCORE_ASC
                    2 -> NameListSortOrder.NAME_ASC
                    3 -> NameListSortOrder.NAME_DESC
                    else -> NameListSortOrder.SCORE_DESC
                }

                if (currentNameListSortOrder != newNameListSortOrder) {
                    currentNameListSortOrder = newNameListSortOrder
                    Log.d(TAG, "Sort order changed to: $currentNameListSortOrder")
                    onSortOrderChanged(newNameListSortOrder)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    enum class NameListSortOrder {
        SCORE_DESC, SCORE_ASC, NAME_ASC, NAME_DESC
    }
}