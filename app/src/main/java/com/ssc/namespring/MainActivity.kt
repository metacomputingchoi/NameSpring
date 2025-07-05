// MainActivity.kt
package com.ssc.namespring

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        uiComponents.btnNaming.setOnClickListener {
            startActivity(Intent(this@MainActivity, NamingActivity::class.java))
        }
        uiComponents.btnEvaluation.setOnClickListener {
            startActivity(Intent(this@MainActivity, EvaluationActivity::class.java))
        }
        uiComponents.btnCompare.setOnClickListener {
            startActivity(Intent(this@MainActivity, CompareActivity::class.java))
        }
        uiComponents.btnHistory.setOnClickListener {
            startActivity(Intent(this@MainActivity, HistoryActivity::class.java))
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