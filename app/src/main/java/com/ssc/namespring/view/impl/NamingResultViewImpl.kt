// view/impl/NamingResultViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.NamingResultView
import com.ssc.namespring.utils.UiHelper

class NamingResultViewImpl(private val activity: Activity) : NamingResultView {

    private val logger = AndroidLogger("NamingResultView")

    override fun showNameCards(names: List<GeneratedName>) {
        if (names.isEmpty()) {
            showEmptyResult()
            return
        }

        logger.d("=== 작명 결과 ===")

        names.forEachIndexed { index, name ->
            val score = UiHelper.getNamebomScore(name)
            val sprout = UiHelper.getSproutIcon(score)

            logger.d("")
            logger.d("[${index + 1}] ${name.surnameHangul}${name.combinedPronounciation} (${name.surnameHanja}${name.combinedHanja})")
            logger.d("   $sprout 점수: ${score}점")

            // 이름의 주요 특징 표시
            val features = UiHelper.extractNameFeatures(name)
            if (features.isNotEmpty()) {
                logger.d("   특징: ${features.joinToString(", ")}")
            }

            // 이름의 의미 표시
            val meaning = name.hanjaDetails.joinToString(" + ") { it.inmyongMeaning }
            logger.d("   의미: $meaning")
        }
    }

    override fun showFavoriteButton(name: GeneratedName, isFavorite: Boolean) {
        // 각 이름 카드에서 표시됨
    }

    override fun showSortOptions() {
        logger.d("")
        logger.d("정렬: [점수순 ▼] [가나다순]")
    }

    override fun showPagination(currentPage: Int, totalPages: Int) {
        logger.d("")
        logger.d("페이지: [$currentPage / $totalPages]  [◀] [▶]")
    }

    override fun showLoadMore(hasMore: Boolean) {
        if (hasMore) {
            logger.d("")
            logger.d("[▼ 더 보기]")
        }
    }

    override fun showResultSummary(totalCount: Int, displayedCount: Int) {
        logger.d("")
        logger.d("총 ${totalCount}개 중 ${displayedCount}개 표시")
    }

    override fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            logger.d("🌱 이름 생성 중...")
        }
    }

    override fun showError(message: String) {
        logger.e("❌ 오류: $message")
    }

    override fun showEmptyResult() {
        logger.d("")
        logger.d("😢 조건에 맞는 이름이 없습니다")
        logger.d("💡 조건을 완화하거나 간편 모드를 시도해보세요")
    }

    override fun updateFavoriteStatus(name: GeneratedName, isFavorite: Boolean) {
        val status = if (isFavorite) "추가됨 ⭐" else "제거됨 ☆"
        logger.d("즐겨찾기 $status: ${name.surnameHangul}${name.combinedPronounciation}")
    }
}