// ui/compare/filter/FilterManager.kt
package com.ssc.namespring.ui.compare.filter

import com.ssc.namespring.model.data.repository.FavoriteName

class FilterManager {
    private val filters = mutableListOf<NameFilter>()
    private var searchFilter: SearchFilter? = null

    fun addFilter(filter: NameFilter) {
        if (filter is SearchFilter) {
            searchFilter = filter
        } else {
            filters.add(filter)
        }
    }

    fun removeFilter(filter: NameFilter) {
        filters.remove(filter)
    }

    fun updateSearchQuery(query: String) {
        searchFilter?.updateQuery(query)
    }

    fun applyFilters(items: List<FavoriteName>): List<FavoriteName> {
        var result = items

        // 검색 필터 먼저 적용
        searchFilter?.let { filter ->
            result = result.filter { filter.matches(it) }
        }

        // 나머지 필터들 적용
        filters.forEach { filter ->
            result = result.filter { filter.matches(it) }
        }

        return result
    }

    fun clearFilters() {
        filters.clear()
    }

    fun getActiveFilterCount(): Int = filters.size
}