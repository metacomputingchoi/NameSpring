// model/data/repository/favorite/FavoriteNameDataSource.kt
package com.ssc.namespring.model.data.repository.favorite

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal class FavoriteNameDataSource(private val context: Context) {

    companion object {
        private const val TAG = "FavoriteNameDataSource"
        private const val FAVORITES_FILE = "favorite_names.json"
        private const val DELETED_FILE = "deleted_favorites.json"
    }

    private val gson = Gson()

    suspend fun loadFavorites(): Map<String, FavoriteName> = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, FAVORITES_FILE)
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<Map<String, FavoriteName>>() {}.type
                val favoritesMap: Map<String, FavoriteName> = gson.fromJson(json, type)
                Log.d(TAG, "Loaded ${favoritesMap.size} favorites")
                favoritesMap.keys.forEach { key ->
                    Log.d(TAG, "Loaded favorite key: $key")
                }
                favoritesMap
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading favorites", e)
            emptyMap()
        }
    }

    suspend fun loadDeletedFavorites(): Map<String, FavoriteName> = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, DELETED_FILE)
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<Map<String, FavoriteName>>() {}.type
                gson.fromJson(json, type)
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading deleted favorites", e)
            emptyMap()
        }
    }

    suspend fun saveFavorites(favorites: Map<String, FavoriteName>) = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, FAVORITES_FILE)
            file.writeText(gson.toJson(favorites))
            Log.d(TAG, "Saved ${favorites.size} favorites to file")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving favorites", e)
        }
    }

    suspend fun saveDeletedFavorites(deleted: Map<String, FavoriteName>) = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, DELETED_FILE)
            file.writeText(gson.toJson(deleted))
        } catch (e: Exception) {
            Log.e(TAG, "Error saving deleted favorites", e)
        }
    }
}