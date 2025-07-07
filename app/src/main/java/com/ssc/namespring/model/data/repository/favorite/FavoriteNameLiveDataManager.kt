// model/data/repository/favorite/FavoriteNameLiveDataManager.kt
package com.ssc.namespring.model.data.repository.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class FavoriteNameLiveDataManager {

    private val _favorites = MutableLiveData<Map<String, FavoriteName>>()
    val favorites: LiveData<Map<String, FavoriteName>> = _favorites

    private val _deletedFavorites = MutableLiveData<Map<String, FavoriteName>>(emptyMap())
    val deletedFavorites: LiveData<Map<String, FavoriteName>> = _deletedFavorites

    suspend fun updateFavorites(favorites: Map<String, FavoriteName>) {
        withContext(Dispatchers.Main) {
            _favorites.value = favorites
        }
    }

    suspend fun updateDeletedFavorites(deletedFavorites: Map<String, FavoriteName>) {
        withContext(Dispatchers.Main) {
            _deletedFavorites.value = deletedFavorites
        }
    }

    fun getCurrentFavorites(): Map<String, FavoriteName> = _favorites.value ?: emptyMap()

    fun getCurrentDeletedFavorites(): Map<String, FavoriteName> = _deletedFavorites.value ?: emptyMap()
}