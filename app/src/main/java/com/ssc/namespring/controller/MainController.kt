// controller/MainController.kt
package com.ssc.namespring.controller

import com.ssc.namespring.model.ProfileModel
import com.ssc.namespring.model.ThemeModel
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.MainView

/**
 * 메인 화면 컨트롤러
 *
 * 역할:
 * - 메인 화면의 비즈니스 로직 처리
 * - Model과 View 사이의 중재자
 * - 프로필 전환, 테마 적용, 네비게이션 처리
 */
class MainController(
    private val profileModel: ProfileModel,
    private val themeModel: ThemeModel,
    private val mainView: MainView
) {

    private val logger = AndroidLogger("MainController")

    private var currentProfile: Profile? = null
    private var allProfiles: List<Profile> = emptyList()
    private var currentProfileIndex = 0

    /**
     * 메인 화면 표시
     * UI 개발 시 이 메서드를 호출하여 화면 초기화
     */
    suspend fun showMainScreen(profile: Profile) {
        try {
            mainView.showLoading(true)

            currentProfile = profile

            // 모든 프로필 로드
            allProfiles = profileModel.getAllProfiles().getOrDefault(emptyList())
            currentProfileIndex = allProfiles.indexOf(profile).coerceAtLeast(0)

            // 테마 적용
            val theme = themeModel.getThemeByScore(profile.namebomScore)
            mainView.applyTheme(theme)

            // 프로필 정보 표시
            mainView.showProfileInfo(profile)

            // 사주 정보 표시
            mainView.showSajuSummary(profile.sajuInfo)

            // 이름의 주요 특징 표시
            val features = extractNameFeatures(profile)
            mainView.showNameFeatures(features)

            // 기능 버튼 활성화
            mainView.enableFeatureButtons()

            // 프로필 전환 가능 표시
            if (allProfiles.size > 1) {
                mainView.showProfileSwitch(allProfiles, currentProfileIndex)
            }

        } catch (e: Exception) {
            logger.e("Failed to show main screen", e)
            mainView.showError("화면 로드 실패: ${e.message}")
        } finally {
            mainView.showLoading(false)
        }
    }

    /**
     * 프로필 전환 처리
     * 좌우 스와이프 시 호출
     */
    suspend fun handleProfileSwitch(direction: SwipeDirection) {
        if (allProfiles.isEmpty()) return

        val oldProfile = currentProfile ?: return

        when (direction) {
            SwipeDirection.LEFT -> {
                currentProfileIndex = (currentProfileIndex - 1 + allProfiles.size) % allProfiles.size
            }
            SwipeDirection.RIGHT -> {
                currentProfileIndex = (currentProfileIndex + 1) % allProfiles.size
            }
        }

        val newProfile = allProfiles[currentProfileIndex]

        // 테마 전환 효과
        val oldTheme = themeModel.getThemeByScore(oldProfile.namebomScore)
        val newTheme = themeModel.getThemeByScore(newProfile.namebomScore)

        if (themeModel.shouldChangeTheme(oldProfile.namebomScore, newProfile.namebomScore)) {
            mainView.showThemeTransition(oldTheme, newTheme)
        }

        // 새 프로필로 화면 갱신
        showMainScreen(newProfile)
    }

    /**
     * 기능 버튼 클릭 처리
     * UI에서 버튼 클릭 시 이 메서드 호출
     */
    fun handleFeatureNavigation(feature: Feature): String? {
        return when (feature) {
            Feature.NAMING -> currentProfile?.id
            Feature.EVALUATION -> currentProfile?.id
            Feature.COMPARISON -> currentProfile?.id
            Feature.HISTORY -> currentProfile?.id
        }
    }

    /**
     * 이름의 특징 추출
     */
    private fun extractNameFeatures(profile: Profile): List<String> {
        val features = mutableListOf<String>()

        // 이름 길이 특징
        when (profile.getTotalNameLength()) {
            2 -> features.add("간결한 두 글자 이름")
            3 -> features.add("전통적인 세 글자 이름")
            4 -> features.add("안정적인 네 글자 이름")
            else -> features.add("특별한 ${profile.getTotalNameLength()}글자 이름")
        }

        // 사주 특징
        profile.sajuInfo?.let { saju ->
            if (saju.isBalanced()) {
                features.add("오행이 균형잡힌 사주")
            } else {
                saju.getMostNeededElements().firstOrNull()?.let { element ->
                    features.add("${element}의 기운이 필요한 사주")
                }
            }
        }

        // 점수 특징
        when (profile.namebomScore) {
            in 80..100 -> features.add("최상급 이름")
            in 60..79 -> features.add("우수한 이름")
            in 40..59 -> features.add("양호한 이름")
            else -> features.add("개선 가능한 이름")
        }

        return features
    }

    enum class SwipeDirection {
        LEFT, RIGHT
    }

    enum class Feature {
        NAMING,      // 작명
        EVALUATION,  // 평가
        COMPARISON,  // 비교
        HISTORY      // 기록
    }
}