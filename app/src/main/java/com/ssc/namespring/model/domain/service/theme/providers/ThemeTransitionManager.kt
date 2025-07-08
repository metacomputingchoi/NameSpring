// model/domain/service/theme/providers/ThemeTransitionManager.kt
package com.ssc.namespring.model.domain.service.theme.providers

import com.ssc.namespring.model.domain.entity.Theme

class ThemeTransitionManager {

    fun getThemeTransitionDuration(): Long = 600L

    fun getSproutAnimationDuration(): Long = 1000L

    fun shouldChangeTheme(oldScore: Int, newScore: Int): Boolean {
        val oldTheme = getThemeByScore(oldScore)
        val newTheme = getThemeByScore(newScore)
        return oldTheme != newTheme
    }

    private fun getThemeByScore(score: Int): Theme {
        return Theme.getAll().find { score in it.scoreRange } ?: Theme.COLD_SPRING
    }
}
