// view/impl/MainViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.data.SajuInfo
import com.ssc.namespring.model.data.Theme
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.MainView
import com.ssc.namespring.utils.JsonLoader

class MainViewImpl(private val activity: Activity) : MainView {

    private val logger = AndroidLogger("MainView")

    override fun applyTheme(theme: Theme) {
        logger.d("=== 테마 적용: ${theme.name} ===")
        logger.d("${theme.themeEmoji} ${theme.description}")
        logger.d("점수 범위: ${theme.scoreRange}")
        logger.d("배경색: ${theme.backgroundColor}")
        logger.d("새싹 상태: ${theme.sproutState}")
    }

    override fun showProfileInfo(profile: Profile) {
        logger.d("=== 현재 프로필 ===")
        logger.d("이름: ${profile.getFullName()} (${profile.getFullHanjaName()})")
        logger.d("프로필명: ${profile.profileName}")
        logger.d("이름봄 점수: ${profile.namebomScore}점")

        // 새싹 아이콘 표시
        val sproutIcon = when (profile.namebomScore) {
            in 80..100 -> "🌸"
            in 60..79 -> "🌳"
            in 40..59 -> "🌿"
            in 20..39 -> "🌱"
            else -> "🌰"
        }
        logger.d("새싹 레벨: $sproutIcon")

        // 점수에 따른 평가 메시지 표시
        val grade = JsonLoader.getGrade(profile.namebomScore)
        val gradeAssessment = JsonLoader.scoreEvaluations.gradeAssessments[grade]
        logger.d("등급: ${grade}등급 - $gradeAssessment")
    }

    override fun showSajuSummary(sajuInfo: SajuInfo?) {
        if (sajuInfo == null) {
            logger.d("사주 정보가 없습니다")
            return
        }

        logger.d("=== 사주 정보 ===")
        logger.d("사주팔자: ${sajuInfo.fourPillars.joinToString(" ")}")
        logger.d(sajuInfo.getElementDescription())

        // 오행 균형 시각화 (텍스트로 표현)
        logger.d("오행 균형:")
        sajuInfo.getElementBalancePercentage().forEach { (element, percentage) ->
            val bar = "█".repeat((percentage / 10).coerceAtLeast(1))
            logger.d("  $element: $bar $percentage%")
        }

        if (sajuInfo.missingElements.isNotEmpty()) {
            logger.d("⚠️ 부족한 오행: ${sajuInfo.missingElements.joinToString(", ")}")

            // 부족한 오행에 대한 보완 방법 표시
            logger.d("💡 보완 방법:")
            sajuInfo.missingElements.forEach { element ->
                val recommendation = JsonLoader.elementCharacteristics.elementLackingRecommendations[element]
                if (recommendation != null) {
                    logger.d("  - $element: $recommendation")
                }

                // 추천 색상도 표시
                val colors = JsonLoader.elementCharacteristics.elementColors[element]
                if (!colors.isNullOrEmpty()) {
                    logger.d("    행운색: ${colors.joinToString(", ")}")
                }
            }
        }
    }

    override fun showNameFeatures(features: List<String>) {
        if (features.isNotEmpty()) {
            logger.d("=== 이름의 특징 ===")
            features.forEach { feature ->
                logger.d("• $feature")
            }
        }
    }

    override fun enableFeatureButtons() {
        logger.d("=== 주요 기능 ===")
        logger.d("[작명] - 새 이름 만들기")
        logger.d("[평가] - 이름 평가하기")
        logger.d("[비교] - 이름 비교하기")
        logger.d("[기록] - 저장된 이름/이력")
    }

    override fun showProfileSwitch(profiles: List<Profile>, currentIndex: Int) {
        logger.d("=== 프로필 전환 ===")
        logger.d("◀ ${if (currentIndex > 0) profiles[currentIndex - 1].profileName else profiles.last().profileName}")
        logger.d("  [${profiles[currentIndex].profileName}]")
        logger.d("  ${if (currentIndex < profiles.size - 1) profiles[currentIndex + 1].profileName else profiles.first().profileName} ▶")
    }

    override fun showThemeTransition(oldTheme: Theme, newTheme: Theme) {
        logger.d("=== 테마 전환 애니메이션 ===")
        logger.d("${oldTheme.name} → ${newTheme.name}")
        logger.d("${oldTheme.themeEmoji} → ${newTheme.themeEmoji}")
    }

    override fun showError(message: String) {
        logger.e("❌ 오류: $message")
    }

    override fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            logger.d("🌱 로딩 중...")
        } else {
            logger.d("✅ 로딩 완료")
        }
    }
}