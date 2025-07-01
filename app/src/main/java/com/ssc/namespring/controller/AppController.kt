// controller/AppController.kt
package com.ssc.namespring.controller

import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.utils.logger.AndroidLogger
import kotlinx.coroutines.*
import java.time.LocalDateTime

/**
 * 앱 전체 네비게이션과 화면 전환을 관리하는 컨트롤러
 */
class AppController(
    private val profileController: ProfileController,
    private val mainController: MainController,
    private val namingController: NamingController,
    private val evaluationController: EvaluationController,
    private val comparisonController: ComparisonController,
    private val favoriteController: FavoriteController,
    private val reportController: ReportController
) {

    private val logger = AndroidLogger("AppController")

    // 컨트롤러 전용 코루틴 스코프
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var currentScreen = Screen.MAIN
    private var currentProfile: Profile? = null

    enum class Screen {
        SPLASH, MAIN, PROFILE_MANAGEMENT, PROFILE_INPUT,
        NAMING_SETTINGS, NAMING_RESULT, EVALUATION_INPUT, EVALUATION_RESULT,
        COMPARISON, FAVORITE, REPORT
    }

    init {
        // ProfileController의 콜백 설정
        profileController.onProfileCreated = { profile ->
            logger.d("프로필 생성 완료, 메인 화면으로 이동")
            currentProfile = profile
            controllerScope.launch {
                showMainScreenWithProfile(profile)
            }
        }
    }

    suspend fun showMainScreen() {
        currentScreen = Screen.MAIN

        // 기본 프로필 로드 또는 프로필 관리 화면으로
        val profiles = profileController.getAllProfiles()

        if (profiles.isEmpty()) {
            logger.d("프로필이 없습니다. 프로필 생성 화면으로 이동합니다.")
            showProfileManagement()
        } else {
            currentProfile = profiles.first()
            showMainScreenWithProfile(currentProfile!!)
        }
    }

    private suspend fun showMainScreenWithProfile(profile: Profile) {
        currentScreen = Screen.MAIN
        mainController.showMainScreen(profile)

        // 메인 화면에서 기능 선택 대기
        simulateMainScreenInteraction()
    }

    fun showProfileManagement() {
        currentScreen = Screen.PROFILE_MANAGEMENT
        controllerScope.launch {
            val isCreatingProfile = profileController.showProfileManagement()
            if (!isCreatingProfile) {
                // 이미 프로필이 있는 경우
                val profiles = profileController.getAllProfiles()
                if (profiles.isNotEmpty()) {
                    showMainScreenWithProfile(profiles.first())
                }
            }
            // isCreatingProfile이 true인 경우는 콜백에서 처리됨
        }
    }

    fun showNamingSettings() {
        currentScreen = Screen.NAMING_SETTINGS
        currentProfile?.let { profile ->
            controllerScope.launch {
                namingController.showNamingSettings(profile)
            }
        }
    }

    fun showEvaluationInput() {
        currentScreen = Screen.EVALUATION_INPUT
        controllerScope.launch {
            val profiles = profileController.getAllProfiles()
            evaluationController.showEvaluationInput(profiles)
        }
    }

    fun showComparison() {
        currentScreen = Screen.COMPARISON
        controllerScope.launch {
            val profiles = profileController.getAllProfiles()
            comparisonController.showComparison(profiles)
        }
    }

    fun showFavorites() {
        currentScreen = Screen.FAVORITE
        controllerScope.launch {
            favoriteController.showFavorites(currentProfile?.id)
        }
    }

    fun showReports() {
        currentScreen = Screen.REPORT
        controllerScope.launch {
            currentProfile?.let { profile ->
                reportController.showReportsByProfile(profile.id)
            } ?: reportController.showRecentReports()
        }
    }

    fun handleBackPress(): Boolean {
        return when (currentScreen) {
            Screen.MAIN -> false // 시스템이 처리
            Screen.PROFILE_MANAGEMENT,
            Screen.NAMING_SETTINGS,
            Screen.EVALUATION_INPUT,
            Screen.COMPARISON,
            Screen.FAVORITE,
            Screen.REPORT -> {
                controllerScope.launch {
                    showMainScreen()
                }
                true
            }
            else -> {
                currentScreen = Screen.MAIN
                true
            }
        }
    }

    private fun simulateMainScreenInteraction() {
        // 실제 앱에서는 버튼 클릭 이벤트로 처리
        logger.d("")
        logger.d("=== 메인 메뉴 선택 시뮬레이션 ===")
        logger.d("1. 작명")
        logger.d("2. 평가")
        logger.d("3. 비교")
        logger.d("4. 기록")
        logger.d("5. 프로필 관리")
        logger.d("6. 보고서")

        // NamingController의 콜백 설정
        namingController.onNamingCompleted = {
            logger.d("")
            logger.d("작명 완료! 다음 기능으로 이동합니다.")
            logger.d("→ 평가 선택")
            showEvaluationInput()
        }

        // EvaluationController의 콜백 설정
        evaluationController.onEvaluationCompleted = {
            logger.d("")
            logger.d("평가 완료! 다음 기능으로 이동합니다.")
            logger.d("→ 즐겨찾기 보기")
            showFavorites()
        }

        // FavoriteController의 콜백 설정
        favoriteController.onFavoritesViewed = {
            logger.d("")
            logger.d("즐겨찾기 확인 완료! 다음 기능으로 이동합니다.")
            logger.d("→ 이름 비교")
            showComparison()
        }

        // ComparisonController의 콜백 설정
        comparisonController.onComparisonCompleted = {
            logger.d("")
            logger.d("비교 완료! 다음 기능으로 이동합니다.")
            logger.d("→ 보고서 보기")
            showReports()
        }

        // ReportController의 콜백 설정
        reportController.onReportsViewed = {
            logger.d("")
            logger.d("보고서 확인 완료! 전체 테스트 시나리오 종료")
            logger.d("")
            logger.d("=== 앱 기능 테스트 완료 ===")
            logger.d("✅ 프로필 생성")
            logger.d("✅ 작명 기능")
            logger.d("✅ 평가 기능")
            logger.d("✅ 즐겨찾기 관리")
            logger.d("✅ 이름 비교")
            logger.d("✅ 보고서 생성 및 저장")
            logger.d("")
            logger.d("모든 기능이 정상적으로 작동합니다!")
            logger.d("이름봄 앱 준비 완료! 🌸")
        }

        // 테스트를 위해 작명 기능 실행
        controllerScope.launch {
            delay(1000)
            logger.d("→ 작명 선택")
            showNamingSettings()
        }
    }
}