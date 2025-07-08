// ui/compare/CompareDataModels.kt
package com.ssc.namespring.ui.compare

import com.ssc.namespring.ui.compare.filter.NameFilter

data class FilterInfo(
    val id: String,
    val type: FilterType,
    val displayName: String,
    val filter: NameFilter
)

data class SortInfo(
    val type: SortType,
    val ascending: Boolean
)