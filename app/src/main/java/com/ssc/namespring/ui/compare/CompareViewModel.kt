// ui/compare/CompareViewModel.kt
package com.ssc.namespring.ui.compare

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namespring.ui.compare.filter.NameFilter
import com.ssc.namespring.ui.compare.manager.*
import com.ssc.namespring.ui.compare.state.FilterStateManager

class CompareViewModel(application: Application) : AndroidViewModel(application) {

    private val favoriteRepository = FavoriteNameRepository.getInstance(application)
    private val selectionManager = SelectionManager()
    private val sortingManager = SortingManager()
    private val filterStateManager = FilterStateManager()
    private val listProcessor = CompareListProcessor(selectionManager, sortingManager, filterStateManager)

    private val _showDeleted = MutableLiveData(false)
    val showDeleted: LiveData<Boolean> = _showDeleted

    private val _searchQuery = MutableLiveData<String?>(null)

    val comparisonList: LiveData<List<FavoriteName>> = selectionManager.comparisonList
    val activeFilters: LiveData<List<FilterInfo>> = filterStateManager.activeFilters
    val activeSorts: LiveData<List<SortInfo>> = sortingManager.activeSorts

    val filteredFavorites = MediatorLiveData<List<FavoriteName>>().apply {
        addSource(favoriteRepository.favorites) { updateFilteredList() }
        addSource(favoriteRepository.deletedFavorites) { updateFilteredList() }
        addSource(_showDeleted) { updateFilteredList() }
        addSource(_searchQuery) { updateFilteredList() }
        addSource(activeFilters) { updateFilteredList() }
        addSource(comparisonList) { updateFilteredList() }
        addSource(activeSorts) { updateFilteredList() }
    }

    private fun updateFilteredList() {
        filteredFavorites.value = if (_showDeleted.value == true) {
            listProcessor.processDeletedList(
                favoriteRepository.getDeletedFavoritesList(),
                _searchQuery.value
            )
        } else {
            listProcessor.processFavoritesList(
                favoriteRepository.getFavoritesList(),
                _searchQuery.value,
                hasActiveConditions()
            )
        }
    }

    private fun hasActiveConditions(): Boolean {
        val hasSearchQuery = !_searchQuery.value.isNullOrEmpty()
        val hasFilters = hasActiveFilters()
        val hasSorts = (activeSorts.value?.isNotEmpty() == true)
        return hasSearchQuery || hasFilters || hasSorts
    }

    fun setShowDeleted(showDeleted: Boolean) {
        _showDeleted.value = showDeleted
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query.ifEmpty { null }
    }

    fun addFilter(type: FilterType, value: Any) = filterStateManager.addFilter(type, value)
    fun removeFilter(filterId: String) = filterStateManager.removeFilter(filterId)
    fun hasActiveFilters(): Boolean = filterStateManager.hasActiveFilters()

    fun toggleSort(type: SortType) = sortingManager.toggleSort(type)
    fun removeSort(type: SortType) = sortingManager.removeSort(type)

    fun toggleFavoriteStatus(favorite: FavoriteName) {
        if (isItemSelected(favorite)) return
        favoriteRepository.removeFavorite(favorite.getKey(), permanently = false)
    }

    fun restoreFavorite(favorite: FavoriteName) {
        favoriteRepository.restoreFavorite(favorite.getKey())
    }

    fun addToComparison(favorite: FavoriteName) {
        selectionManager.addToSelection(favorite)
        updateComparisonListFromSelection()
    }

    fun removeFromComparison(favorite: FavoriteName) {
        selectionManager.removeFromSelection(favorite)
        updateComparisonListFromSelection()
    }

    fun toggleComparison(favorite: FavoriteName) {
        if (favorite.isDeleted) return

        if (isItemSelected(favorite)) {
            removeFromComparison(favorite)
        } else {
            addToComparison(favorite)
        }
    }

    fun isItemSelected(favorite: FavoriteName): Boolean = selectionManager.isItemSelected(favorite)
    fun clearComparison() = selectionManager.clearSelection()
    fun getSelectedItemsOrder(): List<String> = selectionManager.getSelectedItemsOrder()

    private fun updateComparisonListFromSelection() {
        val allFavorites = favoriteRepository.getFavoritesList()
        val selectedFavorites = selectionManager.getSelectedItemsOrder().mapNotNull { key ->
            allFavorites.find { it.getKey() == key }
        }
        selectionManager.setComparisonList(selectedFavorites)
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
        SCORE_RANGE, DATE_RANGE, SURNAME, HANJA_CONTAINS, ELEMENT, MEANING
    }

    enum class SortType {
        NAME, SCORE, BIRTH_DATE, ADDED_DATE
    }
}