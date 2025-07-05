// model/domain/service/ThemeModel.kt
package com.ssc.namespring.model.domain.service

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Theme
import com.ssc.namespring.model.domain.entity.SproutState
import com.ssc.namespring.model.domain.entity.WeatherType

class ThemeModel(private val context: Context) {

    fun getThemeByScore(score: Int): Theme {
        return Theme.getAll().find { score in it.scoreRange } ?: Theme.COLD_SPRING
    }

    fun getThemeTransitionDuration(): Long = 600L

    fun getSproutAnimationDuration(): Long = 1000L

    fun getThemeColors(theme: Theme): ThemeColors {
        val textColor = when (theme.weatherType) {
            WeatherType.SUNNY, WeatherType.WARM -> Color.BLACK
            else -> Color.WHITE
        }

        val secondaryTextColor = when (theme.weatherType) {
            WeatherType.SUNNY, WeatherType.WARM -> Color.DKGRAY
            else -> Color.LTGRAY
        }

        val accentColor = when (theme.sproutState) {
            SproutState.BLOOMING -> ContextCompat.getColor(context, R.color.flower_pink)
            SproutState.GROWING -> ContextCompat.getColor(context, R.color.leaf_green)
            SproutState.SPROUTING -> ContextCompat.getColor(context, R.color.sprout_green)
            SproutState.SEED -> ContextCompat.getColor(context, R.color.seed_brown)
            SproutState.DORMANT -> ContextCompat.getColor(context, R.color.dormant_gray)
        }

        return ThemeColors(
            primary = ContextCompat.getColor(context, theme.primaryColor),
            background = ContextCompat.getColor(context, theme.backgroundColor),
            textPrimary = textColor,
            textSecondary = secondaryTextColor,
            accent = accentColor
        )
    }

    fun shouldChangeTheme(oldScore: Int, newScore: Int): Boolean {
        val oldTheme = getThemeByScore(oldScore)
        val newTheme = getThemeByScore(newScore)
        return oldTheme != newTheme
    }

    fun getSproutIconText(state: SproutState): String {
        return when (state) {
            SproutState.DORMANT -> "🌰"
            SproutState.SEED -> "🌱"
            SproutState.SPROUTING -> "🌿"
            SproutState.GROWING -> "🌳"
            SproutState.BLOOMING -> "🌸"
        }
    }

    fun getSproutIconResource(state: SproutState): Int {
        return when (state) {
            SproutState.DORMANT -> R.drawable.ic_dormant_seed
            SproutState.SEED -> R.drawable.ic_seed
            SproutState.SPROUTING -> R.drawable.ic_sprout
            SproutState.GROWING -> R.drawable.ic_sprout_bloom
            SproutState.BLOOMING -> R.drawable.ic_flower_full
        }
    }

    fun getSproutIconColor(state: SproutState): Int {
        return when (state) {
            SproutState.DORMANT -> R.color.dormant_gray
            SproutState.SEED -> R.color.seed_brown
            SproutState.SPROUTING -> R.color.sprout_green
            SproutState.GROWING -> R.color.leaf_green
            SproutState.BLOOMING -> R.color.flower_pink
        }
    }

    fun getWeatherEffectText(weatherType: WeatherType): String? {
        return when (weatherType) {
            WeatherType.SUNNY -> null
            WeatherType.WARM -> "✨"
            WeatherType.CLOUDY -> "☁️"
            WeatherType.RAINY -> "🌧️"
            WeatherType.COLD -> "❄️"
        }
    }

    fun getWeatherEffectDescription(weatherType: WeatherType): String {
        return when (weatherType) {
            WeatherType.SUNNY -> "맑은 하늘"
            WeatherType.WARM -> "따뜻한 햇살이 비춥니다"
            WeatherType.CLOUDY -> "구름이 끼어있습니다"
            WeatherType.RAINY -> "봄비가 내립니다"
            WeatherType.COLD -> "꽃샘추위가 있습니다"
        }
    }

    fun getThemeMessage(theme: Theme, score: Int): String {
        return when (theme) {
            Theme.SUNNY_SPRING -> "🌸 화창한 봄날처럼 완벽한 이름입니다! (${score}점)"
            Theme.WARM_SPRING -> "🌷 따뜻한 봄햇살 같은 좋은 이름입니다. (${score}점)"
            Theme.CLOUDY_SPRING -> "🌫️ 구름 낀 봄날, 조금 더 개선할 수 있어요. (${score}점)"
            Theme.RAINY_SPRING -> "🌧️ 봄비가 내리네요. 다른 이름도 고려해보세요. (${score}점)"
            Theme.COLD_SPRING -> "❄️ 꽃샘추위가 심하네요. 새로운 이름을 찾아보세요. (${score}점)"
            else -> "이름봄 점수: ${score}점"
        }
    }

    fun getSproutStateDescription(state: SproutState): String {
        return when (state) {
            SproutState.DORMANT -> "씨앗이 땅속에서 잠들어 있어요"
            SproutState.SEED -> "씨앗이 심어졌어요"
            SproutState.SPROUTING -> "새싹이 돋아나고 있어요"
            SproutState.GROWING -> "무럭무럭 자라고 있어요"
            SproutState.BLOOMING -> "아름다운 꽃이 피었어요"
        }
    }

    fun getThemeGradientColors(theme: Theme): Pair<Int, Int> {
        val startColor = ContextCompat.getColor(context, theme.backgroundColor)
        val endColor = theme.backgroundGradientEndColor?.let {
            ContextCompat.getColor(context, it)
        } ?: startColor

        return startColor to endColor
    }

    data class ThemeColors(
        val primary: Int,
        val background: Int,
        val textPrimary: Int,
        val textSecondary: Int,
        val accent: Int
    )
}
