// model/domain/service/theme/providers/ThemeColorProvider.kt
package com.ssc.namespring.model.domain.service.theme.providers

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Theme
import com.ssc.namespring.model.domain.entity.SproutState
import com.ssc.namespring.model.domain.entity.WeatherType

class ThemeColorProvider(private val context: Context) {

    data class ThemeColors(
        val primary: Int,
        val background: Int,
        val textPrimary: Int,
        val textSecondary: Int,
        val accent: Int
    )

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

    fun getThemeGradientColors(theme: Theme): Pair<Int, Int> {
        val startColor = ContextCompat.getColor(context, theme.backgroundColor)
        val endColor = theme.backgroundGradientEndColor?.let {
            ContextCompat.getColor(context, it)
        } ?: startColor

        return startColor to endColor
    }
}
