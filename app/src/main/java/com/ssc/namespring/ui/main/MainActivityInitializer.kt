// ui/main/MainActivityInitializer.kt
package com.ssc.namespring.ui.main

import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.domain.usecase.MainMagager

data class MainActivityComponents(
    val uiComponents: MainUIComponents,
    val uiUpdater: MainUIUpdater,
    val themeManager: MainThemeManager
)

class MainActivityInitializer(
    private val activity: AppCompatActivity,
    private val viewModel: MainMagager
) {
    fun initialize(): MainActivityComponents {
        val uiComponents = MainUIComponents(activity)
        val themeManager = MainThemeManager(activity)
        val uiUpdater = MainUIUpdater(uiComponents, themeManager)

        // ViewModel 관찰 설정
        viewModel.uiState.observe(activity) { state ->
            uiUpdater.updateUI(state)
        }

        return MainActivityComponents(uiComponents, uiUpdater, themeManager)
    }
}