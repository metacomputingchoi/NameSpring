// model/FavoriteModel.kt
package com.ssc.namespring.model

import com.ssc.namespring.model.data.Favorite
import com.ssc.namespring.model.repository.FavoriteRepository
import com.ssc.namingengine.data.GeneratedName
import java.time.LocalDateTime
import java.util.UUID

/**
 * 즐겨찾기 관리 비즈니스 로직
 * GeneratedName의 모든 정보를 보존하여 활용
 */
class FavoriteModel(
    private val repository: FavoriteRepository
) {

    suspend fun addFavorite(
        profileId: String,
        generatedName: GeneratedName,
        memo: String? = null,
        category: String? = null
    ): Result<Favorite> {
        return try {
            val favorite = Favorite(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                generatedName = generatedName,
                memo = memo,
                category = category ?: "미분류"
            )

            repository.insert(favorite)
            Result.success(favorite)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFavorite(id: String): Result<Boolean> {
        return try {
            repository.delete(id)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavorites(): Result<List<Favorite>> {
        return try {
            Result.success(repository.getAll())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoritesByProfile(profileId: String): Result<List<Favorite>> {
        return try {
            Result.success(repository.getByProfileId(profileId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMemo(id: String, memo: String): Result<Favorite> {
        return try {
            val favorite = repository.getById(id)
                ?: return Result.failure(Exception("즐겨찾기를 찾을 수 없습니다"))

            val updated = favorite.copy(
                memo = memo,
                updatedAt = LocalDateTime.now()
            )

            repository.update(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategory(id: String, category: String): Result<Favorite> {
        return try {
            val favorite = repository.getById(id)
                ?: return Result.failure(Exception("즐겨찾기를 찾을 수 없습니다"))

            val updated = favorite.copy(
                category = category,
                updatedAt = LocalDateTime.now()
            )

            repository.update(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRating(id: String, rating: Int): Result<Favorite> {
        return try {
            require(rating in 1..5) { "평점은 1~5 사이여야 합니다" }

            val favorite = repository.getById(id)
                ?: return Result.failure(Exception("즐겨찾기를 찾을 수 없습니다"))

            val updated = favorite.copy(
                rating = rating,
                updatedAt = LocalDateTime.now()
            )

            repository.update(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isFavorite(profileId: String, generatedName: GeneratedName): Boolean {
        return try {
            val favorites = repository.getByProfileId(profileId)
            favorites.any { favorite ->
                // 성씨, 이름, 한자가 모두 같은지 확인
                favorite.generatedName.surnameHangul == generatedName.surnameHangul &&
                favorite.generatedName.surnameHanja == generatedName.surnameHanja &&
                favorite.generatedName.combinedPronounciation == generatedName.combinedPronounciation &&
                favorite.generatedName.combinedHanja == generatedName.combinedHanja
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleFavorite(
        profileId: String,
        generatedName: GeneratedName
    ): Result<Boolean> {
        return try {
            val favorites = repository.getByProfileId(profileId)
            val existing = favorites.find { favorite ->
                favorite.generatedName.surnameHangul == generatedName.surnameHangul &&
                favorite.generatedName.surnameHanja == generatedName.surnameHanja &&
                favorite.generatedName.combinedPronounciation == generatedName.combinedPronounciation &&
                favorite.generatedName.combinedHanja == generatedName.combinedHanja
            }

            if (existing != null) {
                repository.delete(existing.id)
                Result.success(false) // 제거됨
            } else {
                addFavorite(profileId, generatedName)
                Result.success(true) // 추가됨
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoritesByCategory(category: String): Result<List<Favorite>> {
        return try {
            val allFavorites = repository.getAll()
            Result.success(allFavorites.filter { it.category == category })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<String>> {
        return try {
            val allFavorites = repository.getAll()
            val categories = allFavorites.mapNotNull { it.category }.distinct()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoritesByRating(minRating: Int): Result<List<Favorite>> {
        return try {
            val allFavorites = repository.getAll()
            Result.success(allFavorites.filter { (it.rating ?: 0) >= minRating })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 즐겨찾기를 점수 순으로 정렬
     */
    suspend fun getFavoritesSortedByScore(profileId: String): Result<List<Favorite>> {
        return try {
            val favorites = repository.getByProfileId(profileId)
            val sorted = favorites.sortedByDescending { it.getNamebomScore() }
            Result.success(sorted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 즐겨찾기 통계 정보
     */
    suspend fun getFavoriteStatistics(profileId: String): Result<FavoriteStatistics> {
        return try {
            val favorites = repository.getByProfileId(profileId)

            val statistics = FavoriteStatistics(
                totalCount = favorites.size,
                averageScore = favorites.map { it.getNamebomScore() }.average().toInt(),
                categoryCount = favorites.groupBy { it.category }.mapValues { it.value.size },
                ratingDistribution = favorites.groupBy { it.rating ?: 0 }.mapValues { it.value.size },
                topFeatures = favorites.flatMap { it.getMainFeatures() }
                    .groupingBy { it }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .map { it.key to it.value }
            )

            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class FavoriteStatistics(
    val totalCount: Int,
    val averageScore: Int,
    val categoryCount: Map<String?, Int>,
    val ratingDistribution: Map<Int, Int>,
    val topFeatures: List<Pair<String, Int>>
)