// view/impl/NamingResultViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.NamingResultView

class NamingResultViewImpl(private val activity: Activity) : NamingResultView {

    private val logger = AndroidLogger("NamingResultView")

    override fun showNameCards(names: List<GeneratedName>) {
        logger.d("=== 작명 결과: ${names.size}개 ===")

        names.take(10).forEachIndexed { index, name ->
            val score = name.analysisInfo?.totalScore ?: 0
            val sprout = when {
                score >= 140 -> "🌸"
                score >= 120 -> "🌳"
                score >= 100 -> "🌿"
                score >= 80 -> "🌱"
                else -> "🌰"
            }

            logger.d("")
            logger.d("[${index + 1}] $sprout ${name.surnameHangul}${name.combinedPronounciation} (${name.surnameHanja}${name.combinedHanja})")
            logger.d("   점수: ${score}점")

            name.analysisInfo?.let { info ->
                logger.d("   특징: ${info.recommendations.take(2).joinToString(", ")}")
            }
        }

        if (names.size > 10) {
            logger.d("")
            logger.d("... 외 ${names.size - 10}개")
        }
    }

    override fun showFavoriteButton(name: GeneratedName, isFavorite: Boolean) {
        val icon = if (isFavorite) "⭐" else "☆"
        logger.d("$icon 즐겨찾기")
    }

    override fun showSortOptions() {
        logger.d("정렬: [점수순↓] [가나다순]")
    }

    override fun showPagination(currentPage: Int, totalPages: Int) {
        logger.d("페이지: $currentPage / $totalPages")
    }

    override fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            logger.d("이름 생성 중...")
        }
    }

    override fun showError(message: String) {
        logger.e("오류: $message")
    }

    override fun showEmptyResult() {
        logger.d("조건에 맞는 이름이 없습니다")
    }

    override fun updateFavoriteStatus(name: GeneratedName, isFavorite: Boolean) {
        val status = if (isFavorite) "추가됨" else "제거됨"
        logger.d("즐겨찾기 $status: ${name.surnameHangul}${name.combinedPronounciation}")
    }
}