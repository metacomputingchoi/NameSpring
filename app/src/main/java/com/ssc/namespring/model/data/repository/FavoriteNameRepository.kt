package com.ssc.namespring.model.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.ssc.namespring.model.data.repository.favorite.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 기존 패키지 유지를 위한 타입 별칭
typealias FavoriteName = com.ssc.namespring.model.data.repository.favorite.FavoriteName

class FavoriteNameRepository private constructor(
    private val context: Context
) : IFavoriteNameRepository {

    companion object {
        private const val TAG = "FavoriteNameRepository"

        @Volatile
        private var INSTANCE: FavoriteNameRepository? = null

        fun getInstance(context: Context): FavoriteNameRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FavoriteNameRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val dataSource = FavoriteNameDataSource(context)
    private val liveDataManager = FavoriteNameLiveDataManager()

    override val favorites: LiveData<Map<String, FavoriteName>> = liveDataManager.favorites
    override val deletedFavorites: LiveData<Map<String, FavoriteName>> = liveDataManager.deletedFavorites

    init {
        scope.launch {
            liveDataManager.updateFavorites(dataSource.loadFavorites())
            liveDataManager.updateDeletedFavorites(dataSource.loadDeletedFavorites())
        }
    }

    override fun isFavorite(birthDateTime: Long, fullNameKorean: String, fullNameHanja: String): Boolean {
        val key = "$birthDateTime-$fullNameKorean-$fullNameHanja"
        val result = liveDataManager.getCurrentFavorites().containsKey(key)
        Log.d(TAG, "Checking favorite: key=$key, result=$result")
        return result
    }

    override fun addFavorite(favorite: FavoriteName) {
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

    override fun removeFavorite(key: String, permanently: Boolean) {
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

    override fun toggleFavorite(favorite: FavoriteName): Boolean {
        val key = favorite.getKey()
        val fullName = "${favorite.surnameKorean}${favorite.nameKorean}"
        Log.d(TAG, "Toggle - Key: $key, FullName: $fullName")

        val isCurrentlyFavorite = isFavorite(favorite.birthDateTime, favorite.fullNameKorean, favorite.fullNameHanja)
        Log.d(TAG, "Current favorites keys: ${liveDataManager.getCurrentFavorites().keys}")
        Log.d(TAG, "Is currently favorite: $isCurrentlyFavorite")

        if (isCurrentlyFavorite) {
            removeFavorite(key)
        } else {
            addFavorite(favorite)
        }

        return !isCurrentlyFavorite
    }

    override fun restoreFavorite(key: String) {
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

    override fun getFavoritesList(): List<FavoriteName> = liveDataManager.getCurrentFavorites().values.toList()

    override fun getDeletedFavoritesList(): List<FavoriteName> = liveDataManager.getCurrentDeletedFavorites().values.toList()
}