// ui/compare/filter/IFilterHandler.kt
package com.ssc.namespring.ui.compare.filter

import android.view.View
import com.ssc.namespring.model.domain.filter.FilterConfig

interface IFilterHandler {
    fun setupView(view: View)
    fun collectFilters(): List<FilterConfig>
    fun reset()
    fun isEnabled(): Boolean
}