package com.ssc.namespring.model.domain.filter

import com.ssc.namespring.ui.compare.CompareViewModel.FilterType

data class FilterConfig(
    val filterType: FilterType,
    val value: Any
)

data class ScoreRange(
    val min: Int,
    val max: Int
)

data class DateRange(
    val startDate: Long,
    val endDate: Long
)