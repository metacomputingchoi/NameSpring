// ui/history/helpers/HistorySortHelper.kt
package com.ssc.namespring.ui.history.helpers

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.ssc.namespring.ui.history.HistoryFilterManager

object HistorySortHelper {
    fun setupSortSpinner(
        context: Context, 
        spinner: Spinner, 
        onSortChanged: (HistoryFilterManager.HistorySortOrder) -> Unit
    ) {
        val sortOptions = arrayOf(
            "최신순", "오래된순",
            "작업명순", "작업명역순",
            "유형순", "상태순"
        )

        spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, sortOptions)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val historySortOrder = HistoryFilterManager.HistorySortOrder.values()[position]
                onSortChanged(historySortOrder)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}