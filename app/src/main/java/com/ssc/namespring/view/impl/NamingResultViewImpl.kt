// view/impl/NamingResultViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.NamingResultView
import com.ssc.namespring.utils.UiHelper
import com.ssc.namespring.utils.JsonLoader
import java.time.LocalDateTime

class NamingResultViewImpl(private val activity: Activity) : NamingResultView {

    private val logger = AndroidLogger("NamingResultView")
    private var bestScoreSoFar = 0
    private var favoriteCount = 0
    private var attemptCount = 0

    override fun showNameCards(names: List<GeneratedName>) {
        if (names.isEmpty()) {
            showEmptyResult()
            return
        }

        attemptCount++

        // 최고 점수 확인 및 축하 메시지
        val topScore = names.maxOfOrNull { UiHelper.getNamebomScore(it) } ?: 0
        if (topScore > bestScoreSoFar) {
            bestScoreSoFar = topScore
            showCelebrationForHighScore(topScore)
        }

        logger.d("")
        logger.d("=== 작명 결과 ===")

        // 격려 메시지 표시
        if (attemptCount > 5 && bestScoreSoFar < 70) {
            val encouragements = JsonLoader.getEncouragementMessage("no_good_names_yet")
            if (encouragements.isNotEmpty()) {
                logger.d("")
                logger.d("💪 ${encouragements.random()}")
            }
        } else if (attemptCount > 10) {
            val encouragements = JsonLoader.getEncouragementMessage("many_attempts")
            if (encouragements.isNotEmpty()) {
                logger.d("")
                logger.d("👏 ${encouragements.random()}")
            }
        }

        names.forEachIndexed { index, name ->
            val score = UiHelper.getNamebomScore(name)
            val sprout = UiHelper.getSproutIcon(score)

            logger.d("")
            logger.d("[${index + 1}] ${name.surnameHangul}${name.combinedPronounciation} (${name.surnameHanja}${name.combinedHanja})")
            logger.d("   $sprout 점수: ${score}점")

            // 점수별 짧은 평가
            val rangeMessage = JsonLoader.getScoreRangeMessage(score)
            if (rangeMessage != null) {
                logger.d("   ${rangeMessage.emoji} ${rangeMessage.title}")
            }

            // 이름의 주요 특징 표시
            val features = UiHelper.extractNameFeatures(name)
            if (features.isNotEmpty()) {
                logger.d("   특징: ${features.joinToString(", ")}")
            }

            // 이름의 의미 표시 (한자 의미 결합) - 수정됨
            val meanings = name.hanjaDetails.map { hanja ->
                // JSON에서 한자 의미 정보 가져오기
                val hanjaDetail = JsonLoader.getHanjaMeaning(hanja.hanja)
                val meaning = hanjaDetail?.meaning ?: hanja.inmyongMeaning
                val origin = hanjaDetail?.origin ?: ""
                "${hanja.hanja}($meaning${if (origin.isNotEmpty()) " - $origin" else ""})"
            }
            logger.d("   의미: ${meanings.joinToString(" + ")}")

            // 한자 구성 요소 표시
            name.hanjaDetails.forEach { hanja ->
                JsonLoader.getHanjaComponent(hanja.hanja)?.let { component ->
                    if (component.parts.isNotEmpty()) {
                        logger.d("   구성: ${hanja.hanja} = ${component.parts.joinToString("+")}")
                    }
                }
            }

            // lucky_level 기반 사격 정보 표시
            name.sagyeok?.let { sagyeok ->
                val luckyDetails = JsonLoader.getSagyeokLuckyDetails(
                    sagyeok.hyeong,
                    sagyeok.won,
                    sagyeok.i,
                    sagyeok.jeong
                )

                // 사격 평균 점수와 등급 표시
                logger.d("   사격: 평균 ${luckyDetails.averageScore}점")

                // 각 격의 lucky_level 표시
                val luckyLevels = listOf(
                    "형격" to luckyDetails.hyeongLevel,
                    "원격" to luckyDetails.wonLevel,
                    "이격" to luckyDetails.iLevel,
                    "정격" to luckyDetails.jeongLevel
                )

                val bestCount = luckyLevels.count { it.second == "최상운수" }
                val goodCount = luckyLevels.count { it.second in listOf("최상운수", "상운수") }

                if (bestCount > 0) {
                    logger.d("   ⭐ 최상운수 ${bestCount}개 포함")
                } else if (goodCount >= 2) {
                    logger.d("   ✨ 길한 운수가 많습니다")
                }

                // 주격(원격)의 특성 표시
                val wonMeaning = JsonLoader.getStrokeMeaning(sagyeok.won)
                logger.d("   성격: ${wonMeaning.personalityTraits.take(3).joinToString(", ")}")

                // 특별한 운이 있으면 표시
                if (JsonLoader.isBusinessLuckStroke(sagyeok.won)) {
                    logger.d("   💼 사업운이 좋은 이름")
                }
                if (JsonLoader.isLeadershipStroke(sagyeok.hyeong)) {
                    logger.d("   👑 리더십이 뛰어난 이름")
                }
            }

            // 오행 조화 정보 표시
            name.analysisInfo?.ohaengInfo?.let { ohaengInfo ->
                if (ohaengInfo.overallHarmony.contains("조화")) {
                    val elements = (ohaengInfo.baleumOhaeng.toSet() + ohaengInfo.jawonOhaeng.toSet())
                        .filterNot { it.toString().isEmpty() }
                    if (elements.isNotEmpty()) {
                        val elementColors = elements.mapNotNull { element ->
                            JsonLoader.elementCharacteristics.elementColors[element.toString()]?.firstOrNull()
                        }
                        if (elementColors.isNotEmpty()) {
                            logger.d("   행운색: ${elementColors.joinToString(", ")}")
                        }
                    }
                }
            }

            // 부족한 오행 보완 팁 (수정됨)
            name.analysisInfo?.sajuInfo?.missingElements?.forEach { missingElement ->
                JsonLoader.getSajuBasedTips(missingElement)?.let { tips ->
                    tips.tips?.firstOrNull()?.let { tip ->  // nullable 체크
                        logger.d("   💡 $tip")
                    }
                }
            }

            // 인기 테마 한자 체크
            val popularThemes = JsonLoader.namingTips.modernNamingTrends.popularThemes
            popularThemes.forEach { (theme, hanjaList) ->
                if (name.hanjaDetails.any { hanjaList.contains(it.hanja) }) {
                    logger.d("   🏷️ ${theme} 테마")
                }
            }
        }

        // 계절별 특별 메시지
        showSeasonalBonus()
    }

    private fun showCelebrationForHighScore(score: Int) {
        val celebration = JsonLoader.getCelebrationMessage(score)
        if (celebration != null && celebration.messages.isNotEmpty()) {
            logger.d("")
            logger.d("=" * 50)
            logger.d(celebration.messages.random())
            logger.d("=" * 50)
        }

        // 90점 이상 첫 달성
        if (score >= 90 && bestScoreSoFar < 90) {
            JsonLoader.getMilestoneMessage("first_90_score")?.let { message ->
                logger.d("")
                logger.d("🏆 $message")
            }
        }
    }

    private fun showSeasonalBonus() {
        val month = LocalDateTime.now().monthValue
        JsonLoader.getSeasonalMessage(month)?.let { seasonal ->
            logger.d("")
            logger.d("【계절 보너스】")
            logger.d("${seasonal.message}")
            if (seasonal.luckyElements.isNotEmpty()) {
                logger.d("이 계절의 행운 오행: ${seasonal.luckyElements.joinToString(", ")}")
            }
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

        // 점수 향상 메시지
        if (attemptCount > 1 && bestScoreSoFar > 70) {
            val improvements = JsonLoader.getEncouragementMessage("improving_scores")
            if (improvements.isNotEmpty()) {
                logger.d("")
                logger.d("📈 ${improvements.random()}")
            }
        }
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

        // 도움말 표시
        logger.d("")
        logger.d("【작명 팁】")
        JsonLoader.namingTips.generalTips.tips.take(3).forEach { tip ->
            logger.d("• $tip")
        }
    }

    override fun updateFavoriteStatus(name: GeneratedName, isFavorite: Boolean) {
        val status = if (isFavorite) "추가됨 ⭐" else "제거됨 ☆"
        logger.d("즐겨찾기 $status: ${name.surnameHangul}${name.combinedPronounciation}")

        if (isFavorite) {
            favoriteCount++

            // 첫 즐겨찾기 축하
            if (favoriteCount == 1) {
                JsonLoader.getMilestoneMessage("first_favorite")?.let { message ->
                    logger.d("")
                    logger.d("🌱 $message")
                }
            } else if (favoriteCount == 5) {
                JsonLoader.getMilestoneMessage("five_favorites")?.let { message ->
                    logger.d("")
                    logger.d("🌳 $message")
                }
            } else if (favoriteCount == 10) {
                JsonLoader.getMilestoneMessage("ten_favorites")?.let { message ->
                    logger.d("")
                    logger.d("🌲 $message")
                }
            }
        } else {
            favoriteCount--
        }
    }

    companion object {
        private operator fun String.times(count: Int): String = repeat(count)
    }
}