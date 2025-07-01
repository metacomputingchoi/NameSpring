// model/repository/impl/FavoriteRepositoryImpl.kt
package com.ssc.namespring.model.repository.impl

import com.ssc.namespring.model.data.Favorite
import com.ssc.namespring.model.repository.FavoriteRepository
import kotlinx.coroutines.delay

/**
 * 메모리 기반 즐겨찾기 저장소 구현
 * 실제 앱에서는 Room Database로 교체
 */
class FavoriteRepositoryImpl : FavoriteRepository {

    private val favorites = mutableMapOf<String, Favorite>()

    override suspend fun insert(favorite: Favorite) {
        delay(10)
        favorites[favorite.id] = favorite
    }

    override suspend fun update(favorite: Favorite) {
        delay(10)
        favorites[favorite.id] = favorite
    }

    override suspend fun delete(id: String) {
        delay(10)
        favorites.remove(id)
    }

    override suspend fun getById(id: String): Favorite? {
        delay(5)
        return favorites[id]
    }

    override suspend fun getAll(): List<Favorite> {
        delay(5)
        return favorites.values.toList()
    }

    override suspend fun getByProfileId(profileId: String): List<Favorite> {
        delay(5)
        return favorites.values.filter { it.profileId == profileId }
    }

    override suspend fun deleteByProfileId(profileId: String) {
        delay(10)
        favorites.values
            .filter { it.profileId == profileId }
            .forEach { favorites.remove(it.id) }
    }

    override suspend fun search(query: String): List<Favorite> {
        delay(5)
        val lowerQuery = query.lowercase()
        return favorites.values.filter { favorite ->
            favorite.getDisplayName().lowercase().contains(lowerQuery) ||
            favorite.getDisplayHanja().lowercase().contains(lowerQuery) ||
            favorite.memo?.lowercase()?.contains(lowerQuery) == true
        }
    }
}