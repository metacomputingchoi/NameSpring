// model/presentation/adapter/ProfileThemeApplier.kt
package com.ssc.namespring.model.presentation.adapter

import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Profile

class ProfileThemeApplier {

    fun applyTheme(views: ProfileViewReferences, profile: Profile) {
        val theme = profile.getScoreThemeColor()
        val context = views.cardView.context

        val backgroundColor = getBackgroundColor(theme)
        views.cardView.setBackgroundColor(context.getColor(backgroundColor))

        val accentColor = getAccentColor(theme)
        views.scoreContainer.backgroundTintList = context.getColorStateList(accentColor)

        val emojiResId = getEmojiResource(theme)
        views.tvSproutIcon.text = context.getString(emojiResId)
    }

    fun applyNotEvaluatedTheme(views: ProfileViewReferences) {
        val context = views.cardView.context
        views.tvScore.text = "-"
        views.tvSproutIcon.text = context.getString(R.string.icon_dormant_seed)
        views.cardView.setBackgroundColor(context.getColor(R.color.not_evaluated_bg))
        views.scoreContainer.backgroundTintList = context.getColorStateList(R.color.not_evaluated_accent)
        views.tvScore.setTextColor(context.getColor(R.color.text_secondary))
    }

    private fun getBackgroundColor(theme: Profile.ScoreTheme): Int = when (theme) {
        Profile.ScoreTheme.SUNNY_SPRING -> R.color.sunny_spring_bg
        Profile.ScoreTheme.WARM_SPRING -> R.color.warm_spring_bg
        Profile.ScoreTheme.CLOUDY_SPRING -> R.color.cloudy_spring_bg
        Profile.ScoreTheme.RAINY_SPRING -> R.color.rainy_spring_bg
        Profile.ScoreTheme.COLD_SPRING -> R.color.cold_spring_bg
        Profile.ScoreTheme.NOT_EVALUATED -> R.color.not_evaluated_bg
    }

    private fun getAccentColor(theme: Profile.ScoreTheme): Int = when (theme) {
        Profile.ScoreTheme.SUNNY_SPRING -> R.color.sunny_spring_accent
        Profile.ScoreTheme.WARM_SPRING -> R.color.warm_spring_accent
        Profile.ScoreTheme.CLOUDY_SPRING -> R.color.cloudy_spring_accent
        Profile.ScoreTheme.RAINY_SPRING -> R.color.rainy_spring_accent
        Profile.ScoreTheme.COLD_SPRING -> R.color.cold_spring_accent
        Profile.ScoreTheme.NOT_EVALUATED -> R.color.not_evaluated_accent
    }

    private fun getEmojiResource(theme: Profile.ScoreTheme): Int = when (theme) {
        Profile.ScoreTheme.SUNNY_SPRING -> R.string.icon_flower_full
        Profile.ScoreTheme.WARM_SPRING -> R.string.icon_sprout_bloom
        Profile.ScoreTheme.CLOUDY_SPRING -> R.string.icon_sprout
        Profile.ScoreTheme.RAINY_SPRING -> R.string.icon_seed
        Profile.ScoreTheme.COLD_SPRING -> R.string.icon_dormant_seed
        Profile.ScoreTheme.NOT_EVALUATED -> R.string.icon_dormant_seed
    }
}