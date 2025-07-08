// ui/compare/state/FilterStateManager.kt
package com.ssc.namespring.ui.compare.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namespring.ui.compare.FilterInfo
import com.ssc.namespring.ui.compare.FilterType

import com.ssc.namespring.ui.compare.filter.*
import java.util.UUID

class FilterStateManager {
    private val filterManager = FilterManager()
    private val _activeFilters = MutableLiveData<List<FilterInfo>>(emptyList())
    val activeFilters: LiveData<List<FilterInfo>> = _activeFilters

    fun addFilter(type: FilterType, value: Any) {
        val filter = createFilter(type, value)
        filterManager.addFilter(filter)

        val currentFilters = _activeFilters.value?.toMutableList() ?: mutableListOf()
        currentFilters.add(FilterInfo(
            id = UUID.randomUUID().toString(),
            type = type,
            displayName = getFilterDisplayName(type, value),
            filter = filter
        ))
        _activeFilters.value = currentFilters
    }

    fun removeFilter(filterId: String) {
        val currentFilters = _activeFilters.value?.toMutableList() ?: return
        val filterToRemove = currentFilters.find { it.id == filterId } ?: return

        filterManager.removeFilter(filterToRemove.filter)
        currentFilters.remove(filterToRemove)
        _activeFilters.value = currentFilters
    }

    fun hasActiveFilters(): Boolean {
        return (_activeFilters.value?.size ?: 0) > 0
    }

    fun clearFilters() {
        filterManager.clearFilters()
    }

    fun addSearchFilter(query: String) {
        filterManager.clearFilters()
        filterManager.addFilter(SearchFilter(query))
        _activeFilters.value?.forEach { filterInfo ->
            filterManager.addFilter(filterInfo.filter)
        }
    }

    // FavoriteName 타입에 특화된 메서드로 변경
    fun applyFilters(items: List<FavoriteName>): List<FavoriteName> {
        return filterManager.applyFilters(items)
    }

    fun getFilterManager(): FilterManager = filterManager

    private fun createFilter(type: FilterType, value: Any): NameFilter {
        return when (type) {
            FilterType.SCORE_RANGE -> {
                val range = value as IntRange
                ScoreRangeFilter(range.first, range.last)
            }
            FilterType.DATE_RANGE -> {
                val range = value as Pair<Long, Long>
                DateRangeFilter(range.first, range.second)
            }
            FilterType.SURNAME -> SurnameFilter(value as String)
            FilterType.HANJA_CONTAINS -> HanjaContainsFilter(value as String)
            FilterType.ELEMENT -> ElementFilter(value as String)
            FilterType.MEANING -> MeaningFilter(value as String)
        }
    }

    private fun getFilterDisplayName(type: FilterType, value: Any): String {
        return when (type) {
            FilterType.SCORE_RANGE -> {
                val range = value as IntRange
                "점수: ${range.first}~${range.last}"
            }
            FilterType.DATE_RANGE -> "날짜 범위"
            FilterType.SURNAME -> "성씨: $value"
            FilterType.HANJA_CONTAINS -> "한자: $value"
            FilterType.ELEMENT -> "오행: $value"
            FilterType.MEANING -> "의미: $value"
        }
    }
}