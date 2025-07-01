// model/repository/FavoriteRepository.kt
package com.ssc.namespring.model.repository

import com.ssc.namespring.model.data.Favorite

interface FavoriteRepository {
    suspend fun insert(favorite: Favorite)
    suspend fun update(favorite: Favorite)
    suspend fun delete(id: String)
    suspend fun getById(id: String): Favorite?
    suspend fun getAll(): List<Favorite>
    suspend fun getByProfileId(profileId: String): List<Favorite>
    suspend fun deleteByProfileId(profileId: String)
    suspend fun search(query: String): List<Favorite>
}