// ui/compare/CompareActivityEventHandler.kt
package com.ssc.namespring.ui.compare

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.R
import com.ssc.namespring.ui.compare.filter.FilterBottomSheet

class CompareActivityEventHandler(
    private val activity: AppCompatActivity,
    private val components: CompareActivityComponents
) {

    fun onFilterButtonClick() {
        FilterBottomSheet(activity) { filters ->
            filters.forEach { (type, value) ->
                components.initializer.viewModel.addFilter(type, value)
            }
        }.show()
    }

    fun onCompareButtonClick() {
        val comparisonList = components.initializer.viewModel.comparisonList.value ?: emptyList()
        if (comparisonList.size >= 2) {
            CompareResultDialog(activity, comparisonList).show()
        }
    }

    fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity.onBackPressed()
                true
            }
            R.id.action_clear_comparison -> {
                components.initializer.viewModel.clearComparison()
                true
            }
            else -> false
        }
    }
}