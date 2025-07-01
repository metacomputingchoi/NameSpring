// controller/FavoriteController.kt
package com.ssc.namespring.controller

import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.FavoriteModel
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.FavoriteView
import kotlinx.coroutines.*

class FavoriteController(
    private val favoriteModel: FavoriteModel,
    private val favoriteView: FavoriteView
) {

    private val logger = AndroidLogger("FavoriteController")

    // 컨트롤러 전용 코루틴 스코프
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var currentGroupBy = GroupType.NONE

    // 즐겨찾기 보기 완료 콜백
    var onFavoritesViewed: (() -> Unit)? = null

    suspend fun showFavorites(profileId: String? = null) {
        try {
            val favorites = if (profileId != null) {
                favoriteModel.getFavoritesByProfile(profileId).getOrThrow()
            } else {
                favoriteModel.getFavorites().getOrThrow()
            }

            if (favorites.isEmpty()) {
                favoriteView.showEmptyState()
            } else {
                favoriteView.showFavoritesList(favorites)

                // 카테고리 표시
                val categories = favoriteModel.getCategories().getOrThrow()
                if (categories.isNotEmpty()) {
                    favoriteView.showCategories(categories)
                }

                // 필터 및 정렬 옵션
                favoriteView.showFilterOptions()
                favoriteView.showSortOptions()

                // 통계 정보 표시 (테스트)
                showFavoriteStatistics(profileId)
            }

        } catch (e: Exception) {
            favoriteView.showError("즐겨찾기 로드 실패: ${e.message}")
        }
    }

    private suspend fun showFavoriteStatistics(profileId: String?) {
        if (profileId != null) {
            try {
                val statistics = favoriteModel.getFavoriteStatistics(profileId).getOrThrow()

                logger.d("")
                logger.d("=== 즐겨찾기 통계 ===")
                logger.d("총 개수: ${statistics.totalCount}개")
                logger.d("평균 점수: ${statistics.averageScore}점")

                if (statistics.topFeatures.isNotEmpty()) {
                    logger.d("주요 특징:")
                    statistics.topFeatures.forEach { (feature, count) ->
                        logger.d("  - $feature: ${count}개")
                    }
                }

                // 통계 표시 후 다음 기능으로 이동
                controllerScope.launch {
                    delay(2000)
                    onFavoritesViewed?.invoke()
                }

            } catch (e: Exception) {
                logger.e("통계 로드 실패", e)
            }
        }
    }

    suspend fun handleFavoriteToggle(name: GeneratedName, profileId: String) {
        try {
            val result = favoriteModel.toggleFavorite(profileId, name)

            result.onSuccess { added ->
                logger.d("즐겨찾기 ${if (added) "추가" else "제거"}")
                // 목록 갱신
                showFavorites(profileId)
            }.onFailure { error ->
                favoriteView.showError("즐겨찾기 처리 실패: ${error.message}")
            }

        } catch (e: Exception) {
            favoriteView.showError("오류 발생: ${e.message}")
        }
    }

    suspend fun handleMemoUpdate(favoriteId: String, memo: String) {
        try {
            val result = favoriteModel.updateMemo(favoriteId, memo)

            result.onSuccess { updated ->
                favoriteView.updateFavorite(updated)
            }.onFailure { error ->
                favoriteView.showError("메모 업데이트 실패: ${error.message}")
            }

        } catch (e: Exception) {
            favoriteView.showError("오류 발생: ${e.message}")
        }
    }

    suspend fun handleFavoriteGrouping(groupBy: GroupType) {
        currentGroupBy = groupBy

        when (groupBy) {
            GroupType.PROFILE -> {
                // 프로필별 그룹핑은 ProfileRepository도 필요하므로 현재는 스킵
                logger.d("프로필별 그룹핑 (구현 예정)")
            }
            GroupType.CATEGORY -> {
                try {
                    val categories = favoriteModel.getCategories().getOrThrow()
                    categories.forEach { category ->
                        val favorites = favoriteModel.getFavoritesByCategory(category).getOrThrow()
                        logger.d("[$category] ${favorites.size}개")
                    }
                } catch (e: Exception) {
                    favoriteView.showError("그룹핑 실패: ${e.message}")
                }
            }
            GroupType.RATING -> {
                try {
                    for (rating in 5 downTo 1) {
                        val favorites = favoriteModel.getFavoritesByRating(rating).getOrThrow()
                        if (favorites.isNotEmpty()) {
                            logger.d("[${"⭐".repeat(rating)}] ${favorites.size}개")
                        }
                    }
                } catch (e: Exception) {
                    favoriteView.showError("그룹핑 실패: ${e.message}")
                }
            }
            GroupType.NONE -> {
                showFavorites()
            }
        }
    }

    enum class GroupType {
        NONE,      // 그룹핑 없음
        PROFILE,   // 프로필별
        CATEGORY,  // 카테고리별
        RATING     // 평점별
    }
}