// model/data/repository/FavoriteNameRepository.kt
package com.ssc.namespring.model.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class FavoriteName(
    val birthDateTime: Long,
    val nameKorean: String,
    val nameHanja: String,
    val surnameKorean: String,
    val surnameHanja: String,
    val jsonData: String,
    val addedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
) {
    // 전체 이름 (성+이름)
    val fullNameKorean: String get() = "$surnameKorean$nameKorean"
    val fullNameHanja: String get() = "$surnameHanja$nameHanja"

    // 고유 키 생성 - 전체 이름 사용
    fun getKey(): String = "$birthDateTime-$fullNameKorean-$fullNameHanja"
}

class FavoriteNameRepository private constructor(private val context: Context) {

    companion object {
        private const val TAG = "FavoriteNameRepository"
        private const val FAVORITES_FILE = "favorite_names.json"
        private const val DELETED_FILE = "deleted_favorites.json"

        @Volatile
        private var INSTANCE: FavoriteNameRepository? = null

        fun getInstance(context: Context): FavoriteNameRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FavoriteNameRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _favorites = MutableLiveData<Map<String, FavoriteName>>()
    val favorites: LiveData<Map<String, FavoriteName>> = _favorites

    private val _deletedFavorites = MutableLiveData<Map<String, FavoriteName>>(emptyMap())
    val deletedFavorites: LiveData<Map<String, FavoriteName>> = _deletedFavorites

    init {
        loadFavorites()
        loadDeletedFavorites()
    }

    // 즐겨찾기 여부 확인 - 전체 이름으로 확인
    fun isFavorite(birthDateTime: Long, fullNameKorean: String, fullNameHanja: String): Boolean {
        val key = "$birthDateTime-$fullNameKorean-$fullNameHanja"
        val result = _favorites.value?.containsKey(key) ?: false
        Log.d(TAG, "Checking favorite: key=$key, result=$result")
        return result
    }

    // 즐겨찾기 추가 - 메인 스레드에서 안전하게 처리
    fun addFavorite(favorite: FavoriteName) {
        scope.launch {
            val currentFavorites = _favorites.value?.toMutableMap() ?: mutableMapOf()
            val key = favorite.getKey()

            // 메인 스레드에서 LiveData 업데이트
            withContext(Dispatchers.Main) {
                currentFavorites[key] = favorite.copy(isDeleted = false, deletedAt = null)
                _favorites.value = currentFavorites
                Log.d(TAG, "Added favorite: $key, total: ${currentFavorites.size}")
            }

            // 삭제된 목록에서 제거
            val deletedMap = _deletedFavorites.value?.toMutableMap()
            deletedMap?.remove(key)
            withContext(Dispatchers.Main) {
                _deletedFavorites.value = deletedMap
            }

            // 파일 저장은 백그라운드에서
            saveDeletedFavorites(deletedMap ?: emptyMap())
            saveFavorites(currentFavorites)
        }
    }

    // 즐겨찾기 제거 - 메인 스레드에서 안전하게 처리
    fun removeFavorite(key: String, permanently: Boolean = false) {
        scope.launch {
            val currentFavorites = _favorites.value?.toMutableMap() ?: mutableMapOf()
            val favorite = currentFavorites[key]

            if (favorite != null) {
                currentFavorites.remove(key)

                // 메인 스레드에서 LiveData 업데이트
                withContext(Dispatchers.Main) {
                    _favorites.value = currentFavorites
                    Log.d(TAG, "Removed favorite: $key, remaining: ${currentFavorites.size}")
                }

                if (!permanently) {
                    val deletedMap = _deletedFavorites.value?.toMutableMap() ?: mutableMapOf()
                    deletedMap[key] = favorite.copy(
                        isDeleted = true,
                        deletedAt = System.currentTimeMillis()
                    )
                    withContext(Dispatchers.Main) {
                        _deletedFavorites.value = deletedMap
                    }
                    saveDeletedFavorites(deletedMap)
                }

                saveFavorites(currentFavorites)
            }
        }
    }

    // 즐겨찾기 토글 - 동기적으로 처리
    fun toggleFavorite(favorite: FavoriteName): Boolean {
        val key = favorite.getKey()
        val fullName = "${favorite.surnameKorean}${favorite.nameKorean}"
        Log.d(TAG, "Toggle - Key: $key, FullName: $fullName")

        val currentFavorites = _favorites.value ?: emptyMap()
        val isCurrentlyFavorite = currentFavorites.containsKey(key)

        Log.d(TAG, "Current favorites keys: ${currentFavorites.keys}")
        Log.d(TAG, "Is currently favorite: $isCurrentlyFavorite")

        if (isCurrentlyFavorite) {
            removeFavorite(key)
        } else {
            addFavorite(favorite)
        }

        return !isCurrentlyFavorite
    }

    // 삭제된 즐겨찾기 복원 - 메인 스레드에서 안전하게 처리
    fun restoreFavorite(key: String) {
        scope.launch {
            val deletedMap = _deletedFavorites.value?.toMutableMap() ?: mutableMapOf()
            val favorite = deletedMap[key]

            if (favorite != null) {
                deletedMap.remove(key)

                // 메인 스레드에서 LiveData 업데이트
                withContext(Dispatchers.Main) {
                    _deletedFavorites.value = deletedMap
                }

                saveDeletedFavorites(deletedMap)

                // addFavorite 호출 (이미 내부적으로 코루틴 처리됨)
                addFavorite(favorite.copy(isDeleted = false, deletedAt = null))
                Log.d(TAG, "Restored favorite: $key")
            }
        }
    }

    // 즐겨찾기 목록 가져오기
    fun getFavoritesList(): List<FavoriteName> {
        return _favorites.value?.values?.toList() ?: emptyList()
    }

    // 삭제된 즐겨찾기 목록 가져오기
    fun getDeletedFavoritesList(): List<FavoriteName> {
        return _deletedFavorites.value?.values?.toList() ?: emptyList()
    }

    private fun loadFavorites() {
        scope.launch {
            try {
                val file = File(context.filesDir, FAVORITES_FILE)
                if (file.exists()) {
                    val json = file.readText()
                    val type = object : TypeToken<Map<String, FavoriteName>>() {}.type
                    val favoritesMap: Map<String, FavoriteName> = gson.fromJson(json, type)

                    withContext(Dispatchers.Main) {
                        _favorites.value = favoritesMap
                    }

                    Log.d(TAG, "Loaded ${favoritesMap.size} favorites")

                    // 로드된 즐겨찾기 키 로그
                    favoritesMap.keys.forEach { key ->
                        Log.d(TAG, "Loaded favorite key: $key")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _favorites.value = emptyMap()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading favorites", e)
                withContext(Dispatchers.Main) {
                    _favorites.value = emptyMap()
                }
            }
        }
    }

    private fun loadDeletedFavorites() {
        scope.launch {
            try {
                val file = File(context.filesDir, DELETED_FILE)
                if (file.exists()) {
                    val json = file.readText()
                    val type = object : TypeToken<Map<String, FavoriteName>>() {}.type
                    val deletedMap: Map<String, FavoriteName> = gson.fromJson(json, type)

                    withContext(Dispatchers.Main) {
                        _deletedFavorites.value = deletedMap
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _deletedFavorites.value = emptyMap()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading deleted favorites", e)
                withContext(Dispatchers.Main) {
                    _deletedFavorites.value = emptyMap()
                }
            }
        }
    }

    private suspend fun saveFavorites(favorites: Map<String, FavoriteName>) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, FAVORITES_FILE)
                file.writeText(gson.toJson(favorites))
                Log.d(TAG, "Saved ${favorites.size} favorites to file")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving favorites", e)
            }
        }
    }

    private suspend fun saveDeletedFavorites(deleted: Map<String, FavoriteName>) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, DELETED_FILE)
                file.writeText(gson.toJson(deleted))
            } catch (e: Exception) {
                Log.e(TAG, "Error saving deleted favorites", e)
            }
        }
    }
}