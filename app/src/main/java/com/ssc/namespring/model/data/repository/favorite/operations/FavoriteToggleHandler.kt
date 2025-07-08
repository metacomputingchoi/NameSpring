// model/data/repository/favorite/operations/FavoriteToggleHandler.kt
package com.ssc.namespring.model.data.repository.favorite.operations

import android.util.Log
import com.ssc.namespring.model.data.repository.favorite.FavoriteName
import com.ssc.namespring.model.data.repository.favorite.FavoriteNameLiveDataManager

class FavoriteToggleHandler internal constructor(
    private val operations: FavoriteOperations,
    private val liveDataManager: FavoriteNameLiveDataManager
) {
    private val TAG = "FavoriteToggleHandler"

    fun isFavorite(birthDateTime: Long, fullNameKorean: String, fullNameHanja: String): Boolean {
        val key = "$birthDateTime-$fullNameKorean-$fullNameHanja"
        val result = liveDataManager.getCurrentFavorites().containsKey(key)
        Log.d(TAG, "Checking favorite: key=$key, result=$result")
        return result
    }

    fun toggleFavorite(favorite: FavoriteName): Boolean {
        val key = favorite.getKey()
        val fullName = "${favorite.surnameKorean}${favorite.nameKorean}"
        Log.d(TAG, "Toggle - Key: $key, FullName: $fullName")

        val isCurrentlyFavorite = isFavorite(favorite.birthDateTime, favorite.fullNameKorean, favorite.fullNameHanja)
        Log.d(TAG, "Current favorites keys: ${liveDataManager.getCurrentFavorites().keys}")
        Log.d(TAG, "Is currently favorite: $isCurrentlyFavorite")

        if (isCurrentlyFavorite) {
            operations.removeFavorite(key, false)
        } else {
            operations.addFavorite(favorite)
        }

        return !isCurrentlyFavorite
    }
}