// ui/compare/CompareViewModel.kt
package com.ssc.namespring.ui.compare

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namespring.ui.compare.filter.*
import java.util.UUID

class CompareViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CompareViewModel"
    }

    private val favoriteRepository = FavoriteNameRepository.getInstance(application)
    private val filterManager = FilterManager()
    private val gson = com.google.gson.Gson()

    // UI 상태
    private val _showDeleted = MutableLiveData(false)
    val showDeleted: LiveData<Boolean> = _showDeleted

    private val _searchQuery = MutableLiveData<String?>(null)

    // 선택된 항목들과 순서 추적
    private val selectedItemsOrder = mutableListOf<String>()
    private val _comparisonList = MutableLiveData<List<FavoriteName>>(emptyList())
    val comparisonList: LiveData<List<FavoriteName>> = _comparisonList

    private val _activeFilters = MutableLiveData<List<FilterInfo>>(emptyList())
    val activeFilters: LiveData<List<FilterInfo>> = _activeFilters

    // 정렬 상태
    private val _activeSorts = MutableLiveData<List<SortInfo>>(emptyList())
    val activeSorts: LiveData<List<SortInfo>> = _activeSorts

    // 원본 순서를 기억하기 위한 리스트 (절대 변경하지 않음!)
    private val originalOrderList = mutableListOf<String>()
    private var isOriginalOrderInitialized = false

    // 디폴트 상태 시뮬레이션을 위한 선택 해제 히스토리 (항상 유지)
    private val deselectionHistory = mutableListOf<String>()

    // 필터링된 결과
    val filteredFavorites = MediatorLiveData<List<FavoriteName>>().apply {
        addSource(favoriteRepository.favorites) { updateFilteredList() }
        addSource(favoriteRepository.deletedFavorites) { updateFilteredList() }
        addSource(_showDeleted) { updateFilteredList() }
        addSource(_searchQuery) { updateFilteredList() }
        addSource(_activeFilters) { updateFilteredList() }
        addSource(_comparisonList) { updateFilteredList() }
        addSource(_activeSorts) { updateFilteredList() }
    }

    init {
        _activeSorts.value = emptyList()
    }

    private fun updateFilteredList() {
        if (_showDeleted.value == true) {
            // 삭제됨 탭 처리
            val deletedList = favoriteRepository.getDeletedFavoritesList()

            val filtered = if (!_searchQuery.value.isNullOrEmpty()) {
                filterManager.clearFilters()
                filterManager.addFilter(SearchFilter(_searchQuery.value ?: ""))
                filterManager.applyFilters(deletedList)
            } else {
                filterManager.clearFilters()
                deletedList
            }

            val sorted = applySorting(filtered)
            filteredFavorites.value = sorted
        } else {
            // 즐겨찾기 탭
            val baseList = favoriteRepository.getFavoritesList()

            // 원본 순서 저장 (처음 한 번만, 리스트가 비어있지 않을 때)
            if (!isOriginalOrderInitialized && baseList.isNotEmpty()) {
                originalOrderList.clear()
                baseList.forEach { favorite ->
                    originalOrderList.add(favorite.getKey())
                }
                isOriginalOrderInitialized = true
                Log.d(TAG, "Original order initialized: ${originalOrderList.joinToString()}")
            }

            // 선택된 항목과 선택되지 않은 항목 분리
            val selectedKeys = selectedItemsOrder.toSet()
            val selectedItems = mutableListOf<FavoriteName>()
            val unselectedItems = mutableListOf<FavoriteName>()

            baseList.forEach { item ->
                if (selectedKeys.contains(item.getKey())) {
                    selectedItems.add(item)
                } else {
                    unselectedItems.add(item)
                }
            }

            // 선택된 항목을 선택 순서대로 정렬
            selectedItems.sortBy { item ->
                selectedItemsOrder.indexOf(item.getKey())
            }

            // 조건 활성화 여부 확인
            val hasConditions = hasActiveConditions()

            val finalUnselectedList = if (hasConditions) {
                // 조건이 있으면 필터와 정렬 적용
                if (!_searchQuery.value.isNullOrEmpty()) {
                    filterManager.clearFilters()
                    filterManager.addFilter(SearchFilter(_searchQuery.value ?: ""))
                    _activeFilters.value?.forEach { filterInfo ->
                        filterManager.addFilter(filterInfo.filter)
                    }
                }

                val filteredUnselected = if (!_searchQuery.value.isNullOrEmpty() || hasActiveFilters()) {
                    filterManager.applyFilters(unselectedItems)
                } else {
                    unselectedItems
                }

                applySorting(filteredUnselected)
            } else {
                // 디폴트 상태: 시뮬레이션된 순서 적용
                applyDefaultOrderSimulation(unselectedItems)
            }

            val finalList = selectedItems + finalUnselectedList
            Log.d(TAG, "Final list order: ${finalList.map { it.fullNameKorean }.joinToString()}")

            filteredFavorites.value = finalList
        }
    }

    private fun applyDefaultOrderSimulation(unselectedItems: List<FavoriteName>): List<FavoriteName> {
        val recentlyDeselected = mutableListOf<FavoriteName>()
        val normalItems = mutableListOf<FavoriteName>()

        unselectedItems.forEach { item ->
            if (deselectionHistory.contains(item.getKey())) {
                recentlyDeselected.add(item)
            } else {
                normalItems.add(item)
            }
        }

        // 최근 선택 해제된 항목들을 해제 역순으로 정렬 (가장 최근 것이 먼저)
        recentlyDeselected.sortByDescending { item ->
            deselectionHistory.indexOf(item.getKey())
        }

        // 일반 항목들은 원본 순서대로 정렬
        normalItems.sortBy { item ->
            originalOrderList.indexOf(item.getKey())
        }

        // 최근 선택 해제된 항목들을 먼저, 그 다음 일반 항목들
        return recentlyDeselected + normalItems
    }

    private fun hasActiveConditions(): Boolean {
        val hasSearchQuery = !_searchQuery.value.isNullOrEmpty()
        val hasFilters = hasActiveFilters()
        val hasSorts = (_activeSorts.value?.isNotEmpty() == true)

        return hasSearchQuery || hasFilters || hasSorts
    }

    private fun applySorting(list: List<FavoriteName>): List<FavoriteName> {
        val sorts = _activeSorts.value ?: return list
        if (sorts.isEmpty()) return list

        var result = list

        sorts.reversed().forEach { sortInfo ->
            result = when (sortInfo.type) {
                SortType.NAME -> {
                    if (sortInfo.ascending) {
                        result.sortedBy { it.fullNameKorean }
                    } else {
                        result.sortedByDescending { it.fullNameKorean }
                    }
                }
                SortType.SCORE -> {
                    result.sortedBy { favorite ->
                        val score = try {
                            val generatedName = gson.fromJson(
                                favorite.jsonData,
                                com.ssc.namingengine.data.GeneratedName::class.java
                            )
                            generatedName.analysisInfo?.totalScore ?: 0
                        } catch (e: Exception) {
                            0
                        }
                        if (sortInfo.ascending) score else -score
                    }
                }
                SortType.BIRTH_DATE -> {
                    if (sortInfo.ascending) {
                        result.sortedBy { it.birthDateTime }
                    } else {
                        result.sortedByDescending { it.birthDateTime }
                    }
                }
                SortType.ADDED_DATE -> {
                    if (sortInfo.ascending) {
                        result.sortedBy { it.addedAt }
                    } else {
                        result.sortedByDescending { it.addedAt }
                    }
                }
            }
        }

        return result
    }

    fun toggleSort(type: SortType) {
        val currentSorts = _activeSorts.value?.toMutableList() ?: mutableListOf()
        val existingSort = currentSorts.find { it.type == type }

        if (existingSort != null) {
            val index = currentSorts.indexOf(existingSort)
            currentSorts[index] = existingSort.copy(ascending = !existingSort.ascending)
        } else {
            currentSorts.add(SortInfo(type, false))
        }

        _activeSorts.value = currentSorts
    }

    fun removeSort(type: SortType) {
        val currentSorts = _activeSorts.value?.toMutableList() ?: mutableListOf()
        currentSorts.removeAll { it.type == type }
        _activeSorts.value = currentSorts
    }

    fun setShowDeleted(showDeleted: Boolean) {
        _showDeleted.value = showDeleted
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query.ifEmpty { null }
    }

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

    fun toggleFavoriteStatus(favorite: FavoriteName) {
        if (isItemSelected(favorite)) return
        favoriteRepository.removeFavorite(favorite.getKey(), permanently = false)
    }

    fun restoreFavorite(favorite: FavoriteName) {
        favoriteRepository.restoreFavorite(favorite.getKey())
    }

    fun addToComparison(favorite: FavoriteName) {
        if (favorite.isDeleted) return

        val key = favorite.getKey()
        if (!selectedItemsOrder.contains(key)) {
            selectedItemsOrder.add(key)
            // 선택될 때는 선택 해제 히스토리에서 제거 (디폴트 상태 시뮬레이션)
            deselectionHistory.remove(key)
            Log.d(TAG, "Added to comparison: $key")
            updateComparisonList()
        }
    }

    fun removeFromComparison(favorite: FavoriteName) {
        val key = favorite.getKey()

        if (selectedItemsOrder.contains(key)) {
            selectedItemsOrder.remove(key)

            // 조건과 관계없이 항상 선택 해제 히스토리에 추가 (디폴트 상태 시뮬레이션)
            deselectionHistory.add(key)
            Log.d(TAG, "Added to deselection history: $key (조건 유무와 관계없이)")
            Log.d(TAG, "Current deselection history: ${deselectionHistory.joinToString()}")

            Log.d(TAG, "Removed from comparison: $key")
            updateComparisonList()
        }
    }

    fun toggleComparison(favorite: FavoriteName) {
        if (favorite.isDeleted) return

        if (isItemSelected(favorite)) {
            removeFromComparison(favorite)
        } else {
            addToComparison(favorite)
        }
    }

    fun isItemSelected(favorite: FavoriteName): Boolean {
        return selectedItemsOrder.contains(favorite.getKey())
    }

    fun clearComparison() {
        selectedItemsOrder.clear()
        deselectionHistory.clear()
        _comparisonList.value = emptyList()
    }

    private fun updateComparisonList() {
        val allFavorites = favoriteRepository.getFavoritesList()
        val selectedFavorites = selectedItemsOrder.mapNotNull { key ->
            allFavorites.find { it.getKey() == key }
        }
        _comparisonList.value = selectedFavorites
    }

    fun getSelectedItemsOrder(): List<String> {
        return selectedItemsOrder.toList()
    }

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
            FilterType.SURNAME -> {
                SurnameFilter(value as String)
            }
            FilterType.HANJA_CONTAINS -> {
                HanjaContainsFilter(value as String)
            }
            FilterType.ELEMENT -> {
                ElementFilter(value as String)
            }
            FilterType.MEANING -> {
                MeaningFilter(value as String)
            }
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

    enum class FilterType {
        SCORE_RANGE,
        DATE_RANGE,
        SURNAME,
        HANJA_CONTAINS,
        ELEMENT,
        MEANING
    }

    enum class SortType {
        NAME,
        SCORE,
        BIRTH_DATE,
        ADDED_DATE
    }
}