// model/domain/service/theme/ThemeService.kt
package com.ssc.namespring.model.domain.service.theme

import android.content.Context
import com.ssc.namespring.model.domain.entity.Theme
import com.ssc.namespring.model.domain.entity.SproutState
import com.ssc.namespring.model.domain.entity.WeatherType
import com.ssc.namespring.model.domain.service.theme.providers.*

/**
 * 기존 ThemeService의 모든 public 메서드를 유지하여 외부 호환성 보장
 * 내부적으로는 각 책임을 전문 Provider들에게 위임
 */
class ThemeService(private val context: Context) {

    // Provider 인스턴스들 (lazy 초기화로 성능 최적화)
    private val colorProvider by lazy { ThemeColorProvider(context) }
    private val sproutProvider by lazy { SproutStateProvider() }
    private val weatherProvider by lazy { WeatherEffectProvider() }
    private val messageProvider by lazy { ThemeMessageProvider() }
    private val transitionManager by lazy { ThemeTransitionManager() }

    // 기존 public API 유지
    fun getThemeByScore(score: Int): Theme {
        return Theme.getAll().find { score in it.scoreRange } ?: Theme.COLD_SPRING
    }

    fun getThemeTransitionDuration(): Long = transitionManager.getThemeTransitionDuration()

    fun getSproutAnimationDuration(): Long = transitionManager.getSproutAnimationDuration()

    fun getThemeColors(theme: Theme): ThemeColorsService {
        val colors = colorProvider.getThemeColors(theme)
        return ThemeColorsService(
            primary = colors.primary,
            background = colors.background,
            textPrimary = colors.textPrimary,
            textSecondary = colors.textSecondary,
            accent = colors.accent
        )
    }

    fun shouldChangeTheme(oldScore: Int, newScore: Int): Boolean {
        return transitionManager.shouldChangeTheme(oldScore, newScore)
    }

    fun getSproutIconText(state: SproutState): String {
        return sproutProvider.getSproutIconText(state)
    }

    fun getSproutIconResource(state: SproutState): Int {
        return sproutProvider.getSproutIconResource(state)
    }

    fun getSproutIconColor(state: SproutState): Int {
        return sproutProvider.getSproutIconColor(state)
    }

    fun getWeatherEffectText(weatherType: WeatherType): String? {
        return weatherProvider.getWeatherEffectText(weatherType)
    }

    fun getWeatherEffectDescription(weatherType: WeatherType): String {
        return weatherProvider.getWeatherEffectDescription(weatherType)
    }

    fun getThemeMessage(theme: Theme, score: Int): String {
        return messageProvider.getThemeMessage(theme, score)
    }

    fun getSproutStateDescription(state: SproutState): String {
        return sproutProvider.getSproutStateDescription(state)
    }

    fun getThemeGradientColors(theme: Theme): Pair<Int, Int> {
        return colorProvider.getThemeGradientColors(theme)
    }

    // 기존 내부 data class 유지 (외부 호환성)
    data class ThemeColorsService(
        val primary: Int,
        val background: Int,
        val textPrimary: Int,
        val textSecondary: Int,
        val accent: Int
    )
}
