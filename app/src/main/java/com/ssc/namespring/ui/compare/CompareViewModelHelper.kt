// ui/compare/CompareViewModelHelper.kt
package com.ssc.namespring.ui.compare

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.ui.compare.manager.*
import com.ssc.namespring.ui.compare.state.FilterStateManager

class CompareViewModelHelper(
    private val favoriteRepository: FavoriteNameRepository,
    private val selectionManager: SelectionManager,
    private val sortingManager: SortingManager,
    private val filterStateManager: FilterStateManager,
    private val listProcessor: CompareListProcessor
) {
    private val _showDeleted = MutableLiveData(false)
    val showDeleted: LiveData<Boolean> = _showDeleted

    private val _searchQuery = MutableLiveData<String?>(null)
    val searchQuery: LiveData<String?> = _searchQuery

    val filteredFavorites = MediatorLiveData<List<FavoriteName>>()

    private var activeSortsLiveData: LiveData<List<SortInfo>>? = null

    fun setupFilteredFavorites(
        comparisonList: LiveData<List<FavoriteName>>,
        activeFilters: LiveData<List<FilterInfo>>,
        activeSorts: LiveData<List<SortInfo>>
    ) {
        activeSortsLiveData = activeSorts

        filteredFavorites.apply {
            addSource(favoriteRepository.favorites) { updateFilteredList() }
            addSource(favoriteRepository.deletedFavorites) { updateFilteredList() }
            addSource(_showDeleted) { updateFilteredList() }
            addSource(_searchQuery) { updateFilteredList() }
            addSource(activeFilters) { updateFilteredList() }
            addSource(comparisonList) { updateFilteredList() }
            addSource(activeSorts) { updateFilteredList() }
        }
    }

    fun setShowDeleted(showDeleted: Boolean) {
        _showDeleted.value = showDeleted
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query.ifEmpty { null }
    }

    fun hasActiveConditions(): Boolean {
        val hasSearchQuery = !_searchQuery.value.isNullOrEmpty()
        val hasFilters = filterStateManager.hasActiveFilters()
        val hasSorts = (activeSortsLiveData?.value?.isNotEmpty() == true)
        return hasSearchQuery || hasFilters || hasSorts
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

    fun updateComparisonListFromSelection() {
        val allFavorites = favoriteRepository.getFavoritesList()
        val selectedFavorites = selectionManager.getSelectedItemsOrder().mapNotNull { key ->
            allFavorites.find { it.getKey() == key }
        }
        selectionManager.setComparisonList(selectedFavorites)
    }
}