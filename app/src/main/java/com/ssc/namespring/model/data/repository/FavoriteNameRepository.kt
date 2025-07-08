// model/data/repository/FavoriteNameRepository.kt
package com.ssc.namespring.model.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.ssc.namespring.model.data.repository.favorite.*
import com.ssc.namespring.model.data.repository.favorite.operations.FavoriteOperations
import com.ssc.namespring.model.data.repository.favorite.operations.FavoriteToggleHandler

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

    private val dataSource = FavoriteNameDataSource(context)
    private val liveDataManager = FavoriteNameLiveDataManager()
    private val operations = FavoriteOperations(dataSource, liveDataManager)
    private val toggleHandler = FavoriteToggleHandler(operations, liveDataManager)

    override val favorites: LiveData<Map<String, FavoriteName>> = liveDataManager.favorites
    override val deletedFavorites: LiveData<Map<String, FavoriteName>> = liveDataManager.deletedFavorites

    init {
        operations.initializeData()
    }

    override fun isFavorite(birthDateTime: Long, fullNameKorean: String, fullNameHanja: String): Boolean {
        return toggleHandler.isFavorite(birthDateTime, fullNameKorean, fullNameHanja)
    }

    override fun addFavorite(favorite: FavoriteName) {
        operations.addFavorite(favorite)
    }

    override fun removeFavorite(key: String, permanently: Boolean) {
        operations.removeFavorite(key, permanently)
    }

    override fun toggleFavorite(favorite: FavoriteName): Boolean {
        return toggleHandler.toggleFavorite(favorite)
    }

    override fun restoreFavorite(key: String) {
        operations.restoreFavorite(key)
    }

    override fun getFavoritesList(): List<FavoriteName> = liveDataManager.getCurrentFavorites().values.toList()

    override fun getDeletedFavoritesList(): List<FavoriteName> = liveDataManager.getCurrentDeletedFavorites().values.toList()
}