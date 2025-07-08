// ui/compare/filter/strategies/FilterStrategy.kt
package com.ssc.namespring.ui.compare.filter.strategies

import android.view.View
import com.ssc.namespring.ui.compare.CompareViewModel.FilterType

interface FilterStrategy {
    fun createView(): View
    fun getFilterType(): FilterType
    fun extractFilterData(view: View): Any?
    fun validateInput(view: View): Boolean
}