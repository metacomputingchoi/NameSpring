// ui/compare/manager/SelectionManager.kt
package com.ssc.namespring.ui.compare.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.data.repository.FavoriteName

class SelectionManager {
    companion object {
        private const val TAG = "SelectionManager"
    }

    private val selectedItemsOrder = mutableListOf<String>()
    private val deselectionHistory = mutableListOf<String>()
    private val originalOrderList = mutableListOf<String>()
    private var isOriginalOrderInitialized = false

    private val _comparisonList = MutableLiveData<List<FavoriteName>>(emptyList())
    val comparisonList: LiveData<List<FavoriteName>> = _comparisonList

    fun initializeOriginalOrder(items: List<FavoriteName>) {
        if (!isOriginalOrderInitialized && items.isNotEmpty()) {
            originalOrderList.clear()
            items.forEach { favorite ->
                originalOrderList.add(favorite.getKey())
            }
            isOriginalOrderInitialized = true
            Log.d(TAG, "Original order initialized: ${originalOrderList.joinToString()}")
        }
    }

    fun addToSelection(favorite: FavoriteName) {
        if (favorite.isDeleted) return

        val key = favorite.getKey()
        if (!selectedItemsOrder.contains(key)) {
            selectedItemsOrder.add(key)
            deselectionHistory.remove(key)
            Log.d(TAG, "Added to comparison: $key")
            updateComparisonList(favorite)
        }
    }

    fun removeFromSelection(favorite: FavoriteName) {
        val key = favorite.getKey()

        if (selectedItemsOrder.contains(key)) {
            selectedItemsOrder.remove(key)
            deselectionHistory.add(key)
            Log.d(TAG, "Removed from comparison: $key")
            updateComparisonList(favorite)
        }
    }

    fun isItemSelected(favorite: FavoriteName): Boolean {
        return selectedItemsOrder.contains(favorite.getKey())
    }

    fun clearSelection() {
        selectedItemsOrder.clear()
        deselectionHistory.clear()
        _comparisonList.value = emptyList()
    }

    fun getSelectedItemsOrder(): List<String> = selectedItemsOrder.toList()

    fun getDeselectionHistory(): List<String> = deselectionHistory.toList()

    fun getOriginalOrderList(): List<String> = originalOrderList.toList()

    private fun updateComparisonList(favorite: FavoriteName) {
        // This will be updated by ViewModel
        // Keeping it simple to avoid circular dependency
    }

    fun setComparisonList(items: List<FavoriteName>) {
        _comparisonList.value = items
    }
}