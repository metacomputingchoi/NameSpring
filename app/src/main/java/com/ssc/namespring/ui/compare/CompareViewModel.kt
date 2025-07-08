// ui/compare/CompareViewModel.kt
package com.ssc.namespring.ui.compare

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namespring.ui.compare.manager.*
import com.ssc.namespring.ui.compare.state.FilterStateManager

class CompareViewModel(application: Application) : AndroidViewModel(application) {

    private val favoriteRepository = FavoriteNameRepository.getInstance(application)
    private val selectionManager = SelectionManager()
    private val sortingManager = SortingManager()
    private val filterStateManager = FilterStateManager()
    private val listProcessor = CompareListProcessor(selectionManager, sortingManager, filterStateManager)

    private val viewModelHelper = CompareViewModelHelper(
        favoriteRepository,
        selectionManager,
        sortingManager,
        filterStateManager,
        listProcessor
    )

    val showDeleted: LiveData<Boolean> = viewModelHelper.showDeleted
    val comparisonList: LiveData<List<FavoriteName>> = selectionManager.comparisonList
    val activeFilters: LiveData<List<FilterInfo>> = filterStateManager.activeFilters
    val activeSorts: LiveData<List<SortInfo>> = sortingManager.activeSorts
    val filteredFavorites = viewModelHelper.filteredFavorites

    init {
        viewModelHelper.setupFilteredFavorites(comparisonList, activeFilters, activeSorts)
    }

    fun setShowDeleted(showDeleted: Boolean) = viewModelHelper.setShowDeleted(showDeleted)
    fun setSearchQuery(query: String) = viewModelHelper.setSearchQuery(query)

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
        viewModelHelper.updateComparisonListFromSelection()
    }

    fun removeFromComparison(favorite: FavoriteName) {
        selectionManager.removeFromSelection(favorite)
        viewModelHelper.updateComparisonListFromSelection()
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
}