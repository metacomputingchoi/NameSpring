// ui/main/MainThemeManager.kt
package com.ssc.namespring.ui.main

import android.content.Context
import androidx.cardview.widget.CardView
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.utils.ui.ViewUtils

class MainThemeManager(private val context: Context) {

    fun applyTheme(
        components: MainUIComponents,
        theme: Profile.ScoreTheme,
        ohaengCounts: List<Int>
    ) {
        ViewUtils.applyTheme(context, components.rootLayout, components.tvScoreIcon, theme)
        ViewUtils.applyOhaengTheme(context, components.ohaengContainers, theme, ohaengCounts)
        updateServiceButtonsTheme(components.serviceButtons, theme)
        applyScoreContainerTheme(components, theme)
    }

    fun getScoreEmoji(theme: Profile.ScoreTheme): String {
        return when (theme) {
            Profile.ScoreTheme.SUNNY_SPRING -> context.getString(R.string.icon_flower_full)
            Profile.ScoreTheme.WARM_SPRING -> context.getString(R.string.icon_sprout_bloom)
            Profile.ScoreTheme.CLOUDY_SPRING -> context.getString(R.string.icon_sprout)
            Profile.ScoreTheme.RAINY_SPRING -> context.getString(R.string.icon_seed)
            Profile.ScoreTheme.COLD_SPRING -> context.getString(R.string.icon_dormant_seed)
            Profile.ScoreTheme.NOT_EVALUATED -> context.getString(R.string.icon_dormant_seed)
        }
    }

    private fun updateServiceButtonsTheme(buttons: List<CardView>, theme: Profile.ScoreTheme) {
        buttons.forEach { button ->
            button.alpha = 1.0f
        }
    }

    private fun applyScoreContainerTheme(components: MainUIComponents, theme: Profile.ScoreTheme) {
        val accentColor = when (theme) {
            Profile.ScoreTheme.SUNNY_SPRING -> R.color.sunny_spring_accent
            Profile.ScoreTheme.WARM_SPRING -> R.color.warm_spring_accent
            Profile.ScoreTheme.CLOUDY_SPRING -> R.color.cloudy_spring_accent
            Profile.ScoreTheme.RAINY_SPRING -> R.color.rainy_spring_accent
            Profile.ScoreTheme.COLD_SPRING -> R.color.cold_spring_accent
            Profile.ScoreTheme.NOT_EVALUATED -> R.color.not_evaluated_accent
        }

        components.scoreContainer.backgroundTintList = context.getColorStateList(accentColor)

        val scoreTextColor = when (theme) {
            Profile.ScoreTheme.SUNNY_SPRING,
            Profile.ScoreTheme.WARM_SPRING -> R.color.white
            else -> R.color.text_primary
        }
        components.tvScore.setTextColor(context.getColor(scoreTextColor))
    }
}