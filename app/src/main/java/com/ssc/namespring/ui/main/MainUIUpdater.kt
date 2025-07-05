// ui/main/MainUIUpdater.kt
package com.ssc.namespring.ui.main

import com.ssc.namespring.model.domain.usecase.MainUiState

class MainUIUpdater(
    private val components: MainUIComponents,
    private val themeManager: MainThemeManager
) {
    fun updateUI(state: MainUiState) {
        updateTextViews(state)
        updateOhaengCounts(state)
        updateTheme(state)
    }

    private fun updateTextViews(state: MainUiState) {
        components.tvProfileLabel.text = state.profileName
        components.tvScore.text = state.scoreText
        components.tvName.text = state.fullName
        components.tvBirthInfo.text = state.birthInfo
        components.tvOhaengInfo.text = state.ohaengInfo
    }

    private fun updateOhaengCounts(state: MainUiState) {
        state.ohaengCounts.forEachIndexed { index, count ->
            components.ohaengCounts[index].text = count.toString()
        }
    }

    private fun updateTheme(state: MainUiState) {
        val scoreEmoji = themeManager.getScoreEmoji(state.theme)
        components.tvScoreIcon.text = scoreEmoji
        themeManager.applyTheme(components, state.theme, state.ohaengCounts)
    }
}