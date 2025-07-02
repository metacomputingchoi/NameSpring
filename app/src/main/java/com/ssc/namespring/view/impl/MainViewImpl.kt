// view/impl/MainViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.data.SajuInfo
import com.ssc.namespring.model.data.Theme
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.MainView
import com.ssc.namespring.utils.JsonLoader
import com.ssc.namespring.utils.LoggingHelper
import com.ssc.namespring.view.utils.ViewLogger

class MainViewImpl(private val activity: Activity) : MainView {

    private val logger = AndroidLogger("MainView")
    private var isFirstTime = true  // 첫 실행 여부

    override fun applyTheme(theme: Theme) {
        ViewLogger.logSection("테마 적용: ${theme.name}")

        ViewLogger.logResultSummary("테마 정보", listOf(
            "이모지" to theme.themeEmoji,
            "설명" to theme.description,
            "점수 범위" to "${theme.scoreRange.first}-${theme.scoreRange.last}점",
            "새싹 상태" to theme.sproutState.toString(),
            "날씨" to theme.weatherType.toString()
        ))
    }

    override fun showProfileInfo(profile: Profile) {
        // 첫 실행 시 환영 메시지
        if (isFirstTime) {
            showWelcomeMessage()
            isFirstTime = false
        }

        ViewLogger.logSection("현재 프로필")

        // 새싹 아이콘 표시
        val sproutIcon = when (profile.namebomScore) {
            in 80..100 -> "🌸"
            in 60..79 -> "🌳"
            in 40..59 -> "🌿"
            in 20..39 -> "🌱"
            else -> "🌰"
        }

        ViewLogger.logResultSummary("프로필 정보", listOf(
            "이름" to "${profile.getFullName()} (${profile.getFullHanjaName()})",
            "프로필명" to profile.profileName,
            "이름봄 점수" to "${profile.namebomScore}점 $sproutIcon"
        ))

        // 점수에 따른 평가 메시지 표시
        val scoreMessage = JsonLoader.getScoreRangeMessage(profile.namebomScore)
        scoreMessage?.let {
            logger.d("")
            logger.d("${it.emoji} ${it.title}")
            logger.d(it.description)

            if (it.recommendations.isNotEmpty()) {
                ViewLogger.logTipBox(it.recommendations)
            }
        }

        // 점수 해석 가이드
        showScoreInterpretationGuide(profile.namebomScore)

        // 이름봄 점수 툴팁
        JsonLoader.getHelpTooltip("namebom_score")?.let { tooltip ->
            logger.d("")
            logger.d("💡 $tooltip")
        }
    }

    private fun showWelcomeMessage() {
        val welcomeGuide = JsonLoader.getOnboardingGuide("welcome")
        if (welcomeGuide != null) {
            ViewLogger.logBox(listOf(
                welcomeGuide.title,
                "",
                *(welcomeGuide.messages?.toTypedArray() ?: emptyArray())
            ))
        }

        val firstSteps = JsonLoader.getOnboardingGuide("first_steps")
        if (firstSteps != null && firstSteps.steps != null) {
            ViewLogger.logTipBox(firstSteps.steps)
        }
    }

    private fun showScoreInterpretationGuide(score: Int) {
        val interpretationGuide = JsonLoader.userGuideStrings.interpretationGuide["scores"] as? Map<*, *>
        val ranges = interpretationGuide?.get("ranges") as? Map<*, *>

        if (ranges != null) {
            val interpretation = when (score) {
                in 90..100 -> ranges["90_100"]
                in 80..89 -> ranges["80_89"]
                in 70..79 -> ranges["70_79"]
                in 60..69 -> ranges["60_69"]
                in 50..59 -> ranges["50_59"]
                else -> ranges["0_49"]
            }

            if (interpretation != null) {
                logger.d("")
                logger.d("📊 점수 해석: $interpretation")
            }
        }
    }

    override fun showSajuSummary(sajuInfo: SajuInfo?) {
        if (sajuInfo == null) {
            logger.d("사주 정보가 없습니다")
            return
        }

        ViewLogger.logSection("사주 정보")

        ViewLogger.logResultSummary("사주팔자", listOf(
            "연주" to sajuInfo.fourPillars[0],
            "월주" to sajuInfo.fourPillars[1],
            "일주" to sajuInfo.fourPillars[2],
            "시주" to sajuInfo.fourPillars[3]
        ))

        // 사주 정보 툴팁
        JsonLoader.getHelpTooltip("saju_info")?.let { tooltip ->
            logger.d("")
            logger.d("💡 $tooltip")
        }

        // 오행 균형 시각화
        ViewLogger.logScoreVisualization(
            sajuInfo.getElementBalancePercentage().mapValues { (_, percent) ->
                percent
            }
        )

        // 오행 이해하기 가이드
        showElementInterpretationGuide()

        if (sajuInfo.missingElements.isNotEmpty()) {
            val recommendations = mutableListOf<String>()

            sajuInfo.missingElements.forEach { element ->
                JsonLoader.elementCharacteristics.elementLackingRecommendations[element]?.let {
                    recommendations.add("$element: $it")
                }
            }

            ViewLogger.logWarningBox(listOf(
                "부족한 오행: ${sajuInfo.missingElements.joinToString(", ")}",
                *recommendations.toTypedArray()
            ))

            // 추천 색상도 표시
            val colors = sajuInfo.missingElements.flatMap { element ->
                JsonLoader.elementCharacteristics.elementColors[element] ?: emptyList()
            }.distinct()

            if (colors.isNotEmpty()) {
                logger.d("")
                logger.d("🎨 행운의 색상: ${colors.joinToString(", ")}")
            }
        }
    }

    private fun showElementInterpretationGuide() {
        val elementGuide = JsonLoader.userGuideStrings.interpretationGuide["elements"] as? Map<*, *>
        val descriptions = elementGuide?.get("descriptions") as? Map<*, *>

        if (descriptions != null) {
            val tips = descriptions.map { (key, value) -> "$key: $value" }
            ViewLogger.logTipBox(tips)
        }
    }

    override fun showNameFeatures(features: List<String>) {
        if (features.isNotEmpty()) {
            ViewLogger.logResultSummary("이름의 특징",
                features.mapIndexed { index, feature ->
                    "${index + 1}" to feature
                }
            )
        }
    }

    override fun enableFeatureButtons() {
        ViewLogger.logSection("주요 기능", ViewLogger.LineStyle.THICK)

        // 각 기능별 가이드 표시
        val features = listOf("naming", "evaluation", "comparison", "favorites")
        features.forEach { feature ->
            JsonLoader.getFeatureGuide(feature)?.let { guide ->
                logger.d("")
                logger.d("【${guide.title}】")
                logger.d("${guide.description}")
            }
        }

        ViewLogger.logBox(listOf(
            "작명 - 새 이름 만들기",
            "평가 - 이름 평가하기",
            "비교 - 이름 비교하기",
            "기록 - 저장된 이름/이력"
        ))
    }

    override fun showProfileSwitch(profiles: List<Profile>, currentIndex: Int) {
        ViewLogger.logSection("프로필 전환")

        val prevProfile = if (currentIndex > 0) profiles[currentIndex - 1] else profiles.last()
        val currentProfile = profiles[currentIndex]
        val nextProfile = if (currentIndex < profiles.size - 1) profiles[currentIndex + 1] else profiles.first()

        logger.d("◀ ${prevProfile.profileName}")
        logger.d("  [${currentProfile.profileName}]")
        logger.d("  ${nextProfile.profileName} ▶")

        if (profiles.size > 1) {
            logger.d("")
            logger.d("💡 좌우로 스와이프하여 프로필을 전환할 수 있습니다")
        }
    }

    override fun showThemeTransition(oldTheme: Theme, newTheme: Theme) {
        ViewLogger.logSection("테마 전환 애니메이션")
        logger.d("${oldTheme.name} → ${newTheme.name}")
        logger.d("${oldTheme.themeEmoji} → ${newTheme.themeEmoji}")
    }

    override fun showError(message: String) {
        val errorDetails = mutableListOf<String>()

        // 오류 메시지에 따른 도움말
        when {
            message.contains("프로필") -> {
                JsonLoader.getErrorMessage("system_errors", "profile_error")?.let {
                    errorDetails.add(it)
                }
            }
            message.contains("로드") -> {
                JsonLoader.getErrorMessage("system_errors", "load_error")?.let {
                    errorDetails.add(it)
                }
            }
        }

        ViewLogger.logError(message, errorDetails)
    }

    override fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            logger.d("🌱 로딩 중...")
        } else {
            ViewLogger.logSuccess("로딩 완료")
        }
    }
}