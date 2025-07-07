// model/data/repository/favorite/IFavoriteNameRepository.kt
package com.ssc.namespring.model.data.repository.favorite

import androidx.lifecycle.LiveData

interface IFavoriteNameRepository {
    val favorites: LiveData<Map<String, FavoriteName>>
    val deletedFavorites: LiveData<Map<String, FavoriteName>>

    fun isFavorite(birthDateTime: Long, fullNameKorean: String, fullNameHanja: String): Boolean
    fun addFavorite(favorite: FavoriteName)
    fun removeFavorite(key: String, permanently: Boolean = false)
    fun toggleFavorite(favorite: FavoriteName): Boolean
    fun restoreFavorite(key: String)
    fun getFavoritesList(): List<FavoriteName>
    fun getDeletedFavoritesList(): List<FavoriteName>
}