// view/FavoriteView.kt
package com.ssc.namespring.view

import com.ssc.namespring.model.data.Favorite
import com.ssc.namespring.model.data.Profile

interface FavoriteView {
    fun showFavoritesList(favorites: List<Favorite>)
    fun showMemoDialog(favorite: Favorite)
    fun showGroupedByProfile(groupedFavorites: Map<Profile, List<Favorite>>)
    fun showCategories(categories: List<String>)
    fun showFilterOptions()
    fun showSortOptions()
    fun showEmptyState()
    fun showDeleteConfirmation(favorite: Favorite)
    fun updateFavorite(favorite: Favorite)
    fun showError(message: String)
}