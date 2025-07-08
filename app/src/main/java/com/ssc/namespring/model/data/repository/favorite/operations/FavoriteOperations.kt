// model/data/repository/favorite/operations/FavoriteOperations.kt
package com.ssc.namespring.model.data.repository.favorite.operations

import android.util.Log
import com.ssc.namespring.model.data.repository.favorite.FavoriteName
import com.ssc.namespring.model.data.repository.favorite.FavoriteNameDataSource
import com.ssc.namespring.model.data.repository.favorite.FavoriteNameLiveDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteOperations internal constructor(
    private val dataSource: FavoriteNameDataSource,
    private val liveDataManager: FavoriteNameLiveDataManager
) {
    private val TAG = "FavoriteOperations"
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initializeData() {
        scope.launch {
            liveDataManager.updateFavorites(dataSource.loadFavorites())
            liveDataManager.updateDeletedFavorites(dataSource.loadDeletedFavorites())
        }
    }

    fun addFavorite(favorite: FavoriteName) {
        scope.launch {
            val currentFavorites = liveDataManager.getCurrentFavorites().toMutableMap()
            val key = favorite.getKey()

            currentFavorites[key] = favorite.copy(isDeleted = false, deletedAt = null)
            liveDataManager.updateFavorites(currentFavorites)
            Log.d(TAG, "Added favorite: $key, total: ${currentFavorites.size}")

            val deletedMap = liveDataManager.getCurrentDeletedFavorites().toMutableMap()
            deletedMap.remove(key)
            liveDataManager.updateDeletedFavorites(deletedMap)

            dataSource.saveDeletedFavorites(deletedMap)
            dataSource.saveFavorites(currentFavorites)
        }
    }

    fun removeFavorite(key: String, permanently: Boolean) {
        scope.launch {
            val currentFavorites = liveDataManager.getCurrentFavorites().toMutableMap()
            val favorite = currentFavorites[key]

            if (favorite != null) {
                currentFavorites.remove(key)
                liveDataManager.updateFavorites(currentFavorites)
                Log.d(TAG, "Removed favorite: $key, remaining: ${currentFavorites.size}")

                if (!permanently) {
                    val deletedMap = liveDataManager.getCurrentDeletedFavorites().toMutableMap()
                    deletedMap[key] = favorite.copy(isDeleted = true, deletedAt = System.currentTimeMillis())
                    liveDataManager.updateDeletedFavorites(deletedMap)
                    dataSource.saveDeletedFavorites(deletedMap)
                }

                dataSource.saveFavorites(currentFavorites)
            }
        }
    }

    fun restoreFavorite(key: String) {
        scope.launch {
            val deletedMap = liveDataManager.getCurrentDeletedFavorites().toMutableMap()
            val favorite = deletedMap[key]

            if (favorite != null) {
                deletedMap.remove(key)
                liveDataManager.updateDeletedFavorites(deletedMap)
                dataSource.saveDeletedFavorites(deletedMap)
                addFavorite(favorite.copy(isDeleted = false, deletedAt = null))
                Log.d(TAG, "Restored favorite: $key")
            }
        }
    }
}