// MainActivity.kt
package com.ssc.namespring

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ssc.namingengine.NamingEngine
import com.ssc.namespring.controller.*
import com.ssc.namespring.model.*
import com.ssc.namespring.model.repository.impl.*
import com.ssc.namespring.utils.JsonLoader
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.*
import com.ssc.namespring.view.impl.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val logger = AndroidLogger("MainActivity")

    // Models
    private lateinit var namingEngine: NamingEngine
    private lateinit var profileModel: ProfileModel
    private lateinit var nameGeneratorModel: NameGeneratorModel
    private lateinit var favoriteModel: FavoriteModel
    private lateinit var reportModel: ReportModel
    private lateinit var themeModel: ThemeModel

    // Views
    private lateinit var mainView: MainView
    private lateinit var profileManagementView: ProfileManagementView
    private lateinit var profileInputView: ProfileInputView
    private lateinit var namingSettingsView: NamingSettingsView
    private lateinit var namingResultView: NamingResultView
    private lateinit var evaluationInputView: EvaluationInputView
    private lateinit var evaluationResultView: EvaluationResultView
    private lateinit var comparisonView: ComparisonView
    private lateinit var favoriteView: FavoriteView
    private lateinit var reportView: ReportView
    private lateinit var splashView: SplashView

    // Controllers
    private lateinit var appController: AppController
    private lateinit var profileController: ProfileController
    private lateinit var mainController: MainController
    private lateinit var namingController: NamingController
    private lateinit var evaluationController: EvaluationController
    private lateinit var comparisonController: ComparisonController
    private lateinit var favoriteController: FavoriteController
    private lateinit var reportController: ReportController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 스플래시 화면 표시
        showSplash()

        // 컴포넌트 초기화
        lifecycleScope.launch {
            initializeJsonData()
            initializeComponents()
            // 메인 화면으로 이동
            showMainScreen()
        }
    }

    private fun showSplash() {
        splashView = SplashViewImpl(this)
        splashView.showSplashAnimation()
    }

    private suspend fun initializeJsonData() {
        try {
            logger.d("JSON 데이터 초기화 중...")
            JsonLoader.initialize(this)
            logger.d("JSON 데이터 초기화 완료")
        } catch (e: Exception) {
            logger.e("JSON 데이터 초기화 실패", e)
            // 앱을 종료하거나 에러 화면 표시
            finish()
        }
    }

    private suspend fun initializeComponents() {
        try {
            // NamingEngine 초기화
            namingEngine = NamingEngine.create(
                logger = AndroidLogger("NamingEngine")
            )

            // Repository 초기화 (메모리 기반)
            val profileRepository = ProfileRepositoryImpl()
            val favoriteRepository = FavoriteRepositoryImpl()
            val reportRepository = ReportRepositoryImpl()

            // Model 초기화
            profileModel = ProfileModel(profileRepository, namingEngine)
            nameGeneratorModel = NameGeneratorModel(namingEngine)
            favoriteModel = FavoriteModel(favoriteRepository)
            reportModel = ReportModel(reportRepository)
            themeModel = ThemeModel()

            // View 초기화
            mainView = MainViewImpl(this)
            profileManagementView = ProfileManagementViewImpl(this)
            profileInputView = ProfileInputViewImpl(this)
            namingSettingsView = NamingSettingsViewImpl(this)
            namingResultView = NamingResultViewImpl(this)
            evaluationInputView = EvaluationInputViewImpl(this)
            evaluationResultView = EvaluationResultViewImpl(this)
            comparisonView = ComparisonViewImpl(this)
            favoriteView = FavoriteViewImpl(this)
            reportView = ReportViewImpl(this)

            // Controller 초기화
            profileController = ProfileController(
                profileModel = profileModel,
                profileManagementView = profileManagementView,
                profileInputView = profileInputView
            )

            mainController = MainController(
                profileModel = profileModel,
                themeModel = themeModel,
                mainView = mainView
            )

            namingController = NamingController(
                nameGeneratorModel = nameGeneratorModel,
                favoriteModel = favoriteModel,
                namingSettingsView = namingSettingsView,
                namingResultView = namingResultView
            )

            evaluationController = EvaluationController(
                namingEngine = namingEngine,
                reportModel = reportModel,
                evaluationInputView = evaluationInputView,
                evaluationResultView = evaluationResultView
            )

            comparisonController = ComparisonController(
                reportModel = reportModel,
                comparisonView = comparisonView
            )

            favoriteController = FavoriteController(
                favoriteModel = favoriteModel,
                favoriteView = favoriteView
            )

            reportController = ReportController(
                reportModel = reportModel,
                reportView = reportView,
                context = this
            )

            // App Controller 초기화
            appController = AppController(
                profileController = profileController,
                mainController = mainController,
                namingController = namingController,
                evaluationController = evaluationController,
                comparisonController = comparisonController,
                favoriteController = favoriteController,
                reportController = reportController
            )

            logger.d("모든 컴포넌트 초기화 완료")

        } catch (e: Exception) {
            logger.e("초기화 실패", e)
            finish()
        }
    }

    private suspend fun showMainScreen() {
        appController.showMainScreen()
    }

    override fun onBackPressed() {
        if (!appController.handleBackPress()) {
            super.onBackPressed()
        }
    }
}