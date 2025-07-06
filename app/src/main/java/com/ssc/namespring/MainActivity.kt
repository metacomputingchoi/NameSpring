// MainActivity.kt
package com.ssc.namespring

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.entity.ProfileFormMode
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

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainMagager()
        navigationHelper = MainNavigationHelper(this)

        if (!viewModel.hasCurrentProfile()) {
            navigationHelper.navigateToProfileList()
            return
        }

        setContentView(R.layout.activity_main)
        initializeComponents()
        observeViewModel()
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

            // 햅틱 피드백 (옵션)
            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)

            val currentProfile = viewModel.getCurrentProfile()
            currentProfile?.let { profile ->
                val config = ProfileFormConfig(
                    mode = ProfileFormMode.NAMING,
                    parentProfileId = profile.id
                )
                val intent = ProfileFormActivity.newIntent(this, config)
                startActivity(intent)

                // 슬라이드 애니메이션
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

            // 햅틱 피드백 (옵션)
            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)

            val currentProfile = viewModel.getCurrentProfile()
            currentProfile?.let { profile ->
                val config = ProfileFormConfig(
                    mode = ProfileFormMode.EVALUATION,
                    parentProfileId = profile.id
                )
                val intent = ProfileFormActivity.newIntent(this, config)
                startActivity(intent)

                // 슬라이드 애니메이션
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            // 버튼 상태 복구
            handler.postDelayed({
                view.isEnabled = true
                view.alpha = 1.0f
            }, 500)
        }

        uiComponents.btnCompare.setOnClickListener {
            startActivity(Intent(this@MainActivity, CompareActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        uiComponents.btnHistory.setOnClickListener {
            startActivity(Intent(this@MainActivity, HistoryActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            uiUpdater.updateUI(state)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshProfile()
        if (!viewModel.hasCurrentProfile()) {
            navigationHelper.navigateToProfileList()
        }
    }
}