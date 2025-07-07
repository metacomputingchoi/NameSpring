// ui/compare/manager/CompareListProcessor.kt
package com.ssc.namespring.ui.compare.manager

import android.util.Log
import com.ssc.namespring.model.data.repository.FavoriteName

class CompareListProcessor(
    private val selectionManager: SelectionManager,
    private val sortingManager: SortingManager,
    private val filterStateManager: com.ssc.namespring.ui.compare.state.FilterStateManager
) {
    companion object {
        private const val TAG = "CompareListProcessor"
    }

    fun processDeletedList(
        deletedList: List<FavoriteName>,
        searchQuery: String?
    ): List<FavoriteName> {
        val filtered = if (!searchQuery.isNullOrEmpty()) {
            filterStateManager.clearFilters()
            filterStateManager.addSearchFilter(searchQuery)
            filterStateManager.applyFilters(deletedList)
        } else {
            filterStateManager.clearFilters()
            deletedList
        }

        return sortingManager.applySorting(filtered)
    }

    fun processFavoritesList(
        baseList: List<FavoriteName>,
        searchQuery: String?,
        hasActiveConditions: Boolean
    ): List<FavoriteName> {
        selectionManager.initializeOriginalOrder(baseList)

        val selectedKeys = selectionManager.getSelectedItemsOrder().toSet()
        val selectedItems = mutableListOf<FavoriteName>()
        val unselectedItems = mutableListOf<FavoriteName>()

        baseList.forEach { item ->
            if (selectedKeys.contains(item.getKey())) {
                selectedItems.add(item)
            } else {
                unselectedItems.add(item)
            }
        }

        selectedItems.sortBy { item ->
            selectionManager.getSelectedItemsOrder().indexOf(item.getKey())
        }

        val finalUnselectedList = if (hasActiveConditions) {
            processWithConditions(unselectedItems, searchQuery)
        } else {
            applyDefaultOrderSimulation(unselectedItems)
        }

        val finalList = selectedItems + finalUnselectedList
        Log.d(TAG, "Final list order: ${finalList.map { it.fullNameKorean }.joinToString()}")

        return finalList
    }

    private fun processWithConditions(
        unselectedItems: List<FavoriteName>,
        searchQuery: String?
    ): List<FavoriteName> {
        if (!searchQuery.isNullOrEmpty()) {
            filterStateManager.addSearchFilter(searchQuery)
        }

        val filteredUnselected = if (!searchQuery.isNullOrEmpty() || filterStateManager.hasActiveFilters()) {
            filterStateManager.applyFilters(unselectedItems)
        } else {
            unselectedItems
        }

        return sortingManager.applySorting(filteredUnselected)
    }

    private fun applyDefaultOrderSimulation(unselectedItems: List<FavoriteName>): List<FavoriteName> {
        val deselectionHistory = selectionManager.getDeselectionHistory()
        val originalOrderList = selectionManager.getOriginalOrderList()

        val recentlyDeselected = mutableListOf<FavoriteName>()
        val normalItems = mutableListOf<FavoriteName>()

        unselectedItems.forEach { item ->
            if (deselectionHistory.contains(item.getKey())) {
                recentlyDeselected.add(item)
            } else {
                normalItems.add(item)
            }
        }

        recentlyDeselected.sortByDescending { item ->
            deselectionHistory.indexOf(item.getKey())
        }

        normalItems.sortBy { item ->
            originalOrderList.indexOf(item.getKey())
        }

        return recentlyDeselected + normalItems
    }
}