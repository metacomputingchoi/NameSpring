// ui/compare/filter/DateRangeFilter.kt
package com.ssc.namespring.ui.compare.filter

import com.ssc.namespring.model.data.repository.FavoriteName

class DateRangeFilter(
    private val startDate: Long,
    private val endDate: Long
) : NameFilter {

    override fun matches(favorite: FavoriteName): Boolean {
        return favorite.birthDateTime in startDate..endDate
    }
}