// view/impl/FavoriteViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.model.data.Favorite
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.FavoriteView

class FavoriteViewImpl(private val activity: Activity) : FavoriteView {

    private val logger = AndroidLogger("FavoriteView")

    override fun showFavoritesList(favorites: List<Favorite>) {
        logger.d("=== 즐겨찾기 목록 (${favorites.size}개) ===")

        favorites.forEachIndexed { index, favorite ->
            val rating = "⭐".repeat(favorite.rating ?: 0)
            logger.d("")
            logger.d("[${index + 1}] ${favorite.getDisplayName()} (${favorite.getDisplayHanja()})")
            logger.d("   점수: ${favorite.getNamebomScore()}점")
            logger.d("   분류: ${favorite.category ?: "미분류"}")
            if (rating.isNotEmpty()) {
                logger.d("   평점: $rating")
            }
            favorite.memo?.let {
                logger.d("   메모: $it")
            }
        }
    }

    override fun showMemoDialog(favorite: Favorite) {
        logger.d("=== 메모 편집 ===")
        logger.d("이름: ${favorite.getDisplayName()}")
        logger.d("현재 메모: ${favorite.memo ?: "(없음)"}")
        logger.d("[메모 입력란]")
    }

    override fun showGroupedByProfile(groupedFavorites: Map<Profile, List<Favorite>>) {
        logger.d("=== 프로필별 즐겨찾기 ===")

        groupedFavorites.forEach { (profile, favorites) ->
            logger.d("")
            logger.d("【${profile.profileName}】 (${favorites.size}개)")
            favorites.take(3).forEach { favorite ->
                logger.d("  - ${favorite.getDisplayName()} (${favorite.getNamebomScore()}점)")
            }
            if (favorites.size > 3) {
                logger.d("  ... 외 ${favorites.size - 3}개")
            }
        }
    }

    override fun showCategories(categories: List<String>) {
        logger.d("분류: ${categories.joinToString(" | ")}")
    }

    override fun showFilterOptions() {
        logger.d("필터: [전체] [후보] [최종] [검토중]")
    }

    override fun showSortOptions() {
        logger.d("정렬: [최신순] [점수순] [이름순]")
    }

    override fun showEmptyState() {
        logger.d("즐겨찾기가 비어있습니다")
        logger.d("작명 결과에서 🌱 버튼을 눌러 추가하세요")
    }

    override fun showDeleteConfirmation(favorite: Favorite) {
        logger.d("${favorite.getDisplayName()}을(를) 즐겨찾기에서 제거하시겠습니까?")
    }

    override fun updateFavorite(favorite: Favorite) {
        logger.d("즐겨찾기 업데이트: ${favorite.getDisplayName()}")
    }

    override fun showError(message: String) {
        logger.e("오류: $message")
    }
}