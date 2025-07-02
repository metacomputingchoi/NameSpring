// controller/FavoriteController.kt
package com.ssc.namespring.controller

import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.FavoriteModel
import com.ssc.namespring.utils.JsonLoader
import com.ssc.namespring.utils.UiHelper
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.FavoriteView
import com.ssc.namespring.view.utils.ViewLogger
import kotlinx.coroutines.*
import java.time.LocalDateTime

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

            // 계절별 메시지 표시
            showSeasonalMessage()

            if (favorites.isEmpty()) {
                favoriteView.showEmptyState()
                showEmptyFavoritesGuidance()
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

                // 통계 정보 표시
                showFavoriteStatistics(profileId)

                // 즐겨찾기 팁 표시
                showFavoriteTips(favorites)

                // 마일스톤 체크
                checkFavoriteMilestones(favorites.size)
            }

        } catch (e: Exception) {
            favoriteView.showError("즐겨찾기 로드 실패: ${e.message}")
            showFavoriteErrorGuidance()
        }
    }

    /**
     * 계절별 메시지 표시
     */
    private fun showSeasonalMessage() {
        val month = LocalDateTime.now().monthValue
        JsonLoader.getSeasonalMessage(month)?.let { seasonal ->
            logger.d("")
            logger.d("🌿 ${seasonal.message}")
        }
    }

    /**
     * 빈 즐겨찾기 안내
     */
    private fun showEmptyFavoritesGuidance() {
        val encouragements = JsonLoader.getEncouragementMessage("no_good_names_yet")
        if (encouragements.isNotEmpty()) {
            logger.d("")
            logger.d("💪 ${encouragements.random()}")
        }

        logger.d("")
        logger.d("💡 즐겨찾기 사용법:")
        logger.d("• 작명 결과에서 마음에 드는 이름의 🌱 버튼을 누르세요")
        logger.d("• 평가한 이름도 즐겨찾기에 추가할 수 있습니다")
        logger.d("• 메모와 별점으로 관리하세요")
    }

    /**
     * 즐겨찾기 오류 안내
     */
    private fun showFavoriteErrorGuidance() {
        JsonLoader.getErrorMessage("system_errors", "favorite_error")?.let {
            logger.d("💡 $it")
        }
    }

    /**
     * 즐겨찾기 팁 표시
     */
    private fun showFavoriteTips(favorites: List<com.ssc.namespring.model.data.Favorite>) {
        // 평균 점수가 낮은 경우
        val avgScore = favorites.map { it.getNamebomScore() }.average()
        if (avgScore < 70) {
            val encouragements = JsonLoader.getEncouragementMessage("improving_scores")
            if (encouragements.isNotEmpty()) {
                logger.d("")
                logger.d("📈 ${encouragements.random()}")
            }
        }

        // 카테고리 사용 안내
        if (favorites.all { it.category == "미분류" } && favorites.size >= 5) {
            logger.d("")
            logger.d("💡 카테고리로 즐겨찾기를 정리해보세요!")
            logger.d("   예: '최종 후보', '가족 추천', '의미 좋음' 등")
        }
    }

    /**
     * 즐겨찾기 마일스톤 체크
     */
    private fun checkFavoriteMilestones(count: Int) {
        when (count) {
            1 -> JsonLoader.getMilestoneMessage("first_favorite")?.let {
                ViewLogger.logCelebration(it, "🌱")
            }
            5 -> JsonLoader.getMilestoneMessage("five_favorites")?.let {
                ViewLogger.logCelebration(it, "🌳")
            }
            10 -> JsonLoader.getMilestoneMessage("ten_favorites")?.let {
                ViewLogger.logCelebration(it, "🌲")
            }
        }
    }

    private suspend fun showFavoriteStatistics(profileId: String?) {
        if (profileId != null) {
            try {
                val statistics = favoriteModel.getFavoriteStatistics(profileId).getOrThrow()

                ViewLogger.logSection("즐겨찾기 통계", ViewLogger.LineStyle.THIN)

                ViewLogger.logResultSummary("통계 정보", listOf(
                    "총 개수" to "${statistics.totalCount}개",
                    "평균 점수" to "${statistics.averageScore}점 ${UiHelper.getSproutIcon(statistics.averageScore)}",
                    "카테고리 수" to "${statistics.categoryCount.size}개"
                ))

                if (statistics.topFeatures.isNotEmpty()) {
                    logger.d("")
                    logger.d("【주요 특징 TOP 5】")
                    statistics.topFeatures.forEachIndexed { index, (feature, count) ->
                        logger.d("${index + 1}. $feature (${count}개)")
                    }
                }

                // 점수 분포 시각화
                if (statistics.totalCount >= 5) {
                    val distribution = statistics.ratingDistribution
                    logger.d("")
                    logger.d("【평점 분포】")
                    for (rating in 5 downTo 1) {
                        val count = distribution[rating] ?: 0
                        val bar = UiHelper.createMiniProgressBar(count, statistics.totalCount)
                        logger.d("${"⭐".repeat(rating)} $bar ${count}개")
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

                if (added) {
                    // 축하 메시지
                    val score = UiHelper.getNamebomScore(name)
                    if (score >= 90) {
                        logger.d("🎉 훌륭한 이름을 즐겨찾기에 추가했습니다!")
                    }
                }

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
                logger.d("📝 메모가 저장되었습니다")
            }.onFailure { error ->
                favoriteView.showError("메모 업데이트 실패: ${error.message}")
            }

        } catch (e: Exception) {
            favoriteView.showError("오류 발생: ${e.message}")
        }
    }

    suspend fun handleFavoriteGrouping(groupBy: GroupType) {
        currentGroupBy = groupBy

        ViewLogger.logSection("그룹별 정렬: ${groupBy.name}")

        when (groupBy) {
            GroupType.PROFILE -> {
                // 프로필별 그룹핑은 ProfileRepository도 필요하므로 현재는 스킵
                logger.d("프로필별 그룹핑 (구현 예정)")
            }
            GroupType.CATEGORY -> {
                try {
                    val categories = favoriteModel.getCategories().getOrThrow()

                    ViewLogger.logResultSummary("카테고리별 즐겨찾기",
                        categories.map { category ->
                            val favorites = favoriteModel.getFavoritesByCategory(category).getOrThrow()
                            category to "${favorites.size}개"
                        }
                    )
                } catch (e: Exception) {
                    favoriteView.showError("그룹핑 실패: ${e.message}")
                }
            }
            GroupType.RATING -> {
                try {
                    val ratingGroups = mutableListOf<Pair<String, String>>()

                    for (rating in 5 downTo 1) {
                        val favorites = favoriteModel.getFavoritesByRating(rating).getOrThrow()
                        if (favorites.isNotEmpty()) {
                            ratingGroups.add("${"⭐".repeat(rating)}" to "${favorites.size}개")
                        }
                    }

                    ViewLogger.logResultSummary("평점별 즐겨찾기", ratingGroups)
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