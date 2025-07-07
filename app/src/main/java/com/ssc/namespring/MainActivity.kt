// MainActivity.kt
package com.ssc.namespring

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.entity.ProfileFormMode
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.model.domain.service.workmanager.TaskWorkManager
import com.ssc.namespring.model.domain.usecase.MainMagager
import com.ssc.namespring.ui.main.MainNavigationHelper
import com.ssc.namespring.ui.main.MainThemeManager
import com.ssc.namespring.ui.main.MainUIComponents
import com.ssc.namespring.ui.main.MainUIUpdater

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainMagager
    private lateinit var uiComponents: MainUIComponents
    private lateinit var uiUpdater: MainUIUpdater
    private lateinit var themeManager: MainThemeManager
    private lateinit var navigationHelper: MainNavigationHelper
    private lateinit var taskWorkManager: TaskWorkManager

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainMagager()
        navigationHelper = MainNavigationHelper(this)
        taskWorkManager = TaskWorkManager.getInstance(this)

        if (!viewModel.hasCurrentProfile()) {
            navigationHelper.navigateToProfileList()
            return
        }

        setContentView(R.layout.activity_main)
        initializeComponents()
        observeViewModel()

        // 초기 작업 진행 상태 관찰 설정
        viewModel.getCurrentProfile()?.let { profile ->
            observeTaskProgress(profile.id)
        }
    }

    private fun initializeComponents() {
        uiComponents = MainUIComponents(this)
        themeManager = MainThemeManager(this)
        uiUpdater = MainUIUpdater(uiComponents, themeManager)

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        uiComponents.btnNaming.setOnClickListener { view ->
            // 즉시 시각적 피드백
            view.isEnabled = false
            view.alpha = 0.6f
            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)

            val currentProfile = viewModel.getCurrentProfile()
            currentProfile?.let { profile ->
                val config = ProfileFormConfig(
                    mode = ProfileFormMode.NAMING,
                    parentProfileId = profile.id
                )
                val intent = ProfileFormActivity.newIntent(this, config)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            // 버튼 상태 복구
            handler.postDelayed({
                view.isEnabled = true
                view.alpha = 1.0f
            }, 500)
        }

        uiComponents.btnEvaluation.setOnClickListener { view ->
            // 즉시 시각적 피드백
            view.isEnabled = false
            view.alpha = 0.6f
            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)

            val currentProfile = viewModel.getCurrentProfile()
            currentProfile?.let { profile ->
                val config = ProfileFormConfig(
                    mode = ProfileFormMode.EVALUATION,
                    parentProfileId = profile.id
                )
                val intent = ProfileFormActivity.newIntent(this, config)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            // 버튼 상태 복구
            handler.postDelayed({
                view.isEnabled = true
                view.alpha = 1.0f
            }, 500)
        }

        uiComponents.btnCompare.setOnClickListener { view ->
            // 즉시 시각적 피드백
            view.isEnabled = false
            view.alpha = 0.6f
            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)

            // 현재 프로필 ID를 전달하여 비교 대상으로 자동 선택
            val currentProfile = viewModel.getCurrentProfile()
            val intent = Intent(this@MainActivity, CompareActivity::class.java).apply {
                currentProfile?.let {
                    putExtra("initial_profile_id", it.id)
                }
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            // 버튼 상태 복구
            handler.postDelayed({
                view.isEnabled = true
                view.alpha = 1.0f
            }, 300)
        }

        uiComponents.btnHistory.setOnClickListener { view ->
            // 즉시 시각적 피드백
            view.isEnabled = false
            view.alpha = 0.6f
            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)

            // 현재 프로필의 작업 기록을 보기 위해 profile_id 전달
            val currentProfile = viewModel.getCurrentProfile()
            val intent = Intent(this@MainActivity, HistoryActivity::class.java).apply {
                currentProfile?.let {
                    putExtra("profile_id", it.id)
                }
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            // 버튼 상태 복구
            handler.postDelayed({
                view.isEnabled = true
                view.alpha = 1.0f
            }, 300)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            // UIUpdater가 UI 업데이트와 테마 적용을 모두 처리
            uiUpdater.updateUI(state)
        }
    }

    private fun observeTaskProgress(profileId: String) {
        // 현재 프로필의 작업 상태를 관찰
        taskWorkManager.taskHistory.observe(this) { taskHistoryMap ->
            val history = taskHistoryMap[profileId]
            val activeTasks = history?.getActiveTasks() ?: emptyList()

            if (activeTasks.isNotEmpty()) {
                // 활성 작업이 있으면 UI 업데이트 (예: 진행 중 표시)
                updateTaskIndicator(activeTasks)
            } else {
                // 활성 작업이 없으면 버튼 텍스트 원복
                resetTaskIndicator()
            }
        }
    }

    private fun updateTaskIndicator(activeTasks: List<Task>) {
        activeTasks.forEach { task ->
            when (task.type) {
                TaskType.NAMING -> {
                    uiComponents.updateButtonText(
                        MainUIComponents.ButtonType.NAMING,
                        "작명 (진행중...)"
                    )
                }
                TaskType.EVALUATION -> {
                    uiComponents.updateButtonText(
                        MainUIComponents.ButtonType.EVALUATION,
                        "평가 (진행중...)"
                    )
                }
                else -> {}
            }
        }
    }

    private fun resetTaskIndicator() {
        uiComponents.resetAllButtonTexts()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshProfile()
        if (!viewModel.hasCurrentProfile()) {
            navigationHelper.navigateToProfileList()
        } else {
            // 프로필이 있으면 작업 진행 상태 다시 관찰
            viewModel.getCurrentProfile()?.let { profile ->
                observeTaskProgress(profile.id)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}