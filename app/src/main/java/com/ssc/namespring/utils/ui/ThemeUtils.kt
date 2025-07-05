// utils/ui/ThemeUtils.kt
package com.ssc.namespring.utils.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Profile

object ThemeUtils {

    fun applyTheme(
        context: Context,
        rootLayout: ConstraintLayout,
        tvScoreIcon: TextView,
        theme: Profile.ScoreTheme
    ) {
        when (theme) {
            Profile.ScoreTheme.SUNNY_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_sunny_spring)
                tvScoreIcon.text = context.getString(R.string.icon_flower_full)
            }
            Profile.ScoreTheme.WARM_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_warm_spring)
                tvScoreIcon.text = context.getString(R.string.icon_sprout_bloom)
            }
            Profile.ScoreTheme.CLOUDY_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_cloudy_spring)
                tvScoreIcon.text = context.getString(R.string.icon_sprout)
            }
            Profile.ScoreTheme.RAINY_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_rainy_spring)
                tvScoreIcon.text = context.getString(R.string.icon_seed)
            }
            Profile.ScoreTheme.COLD_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_cold_spring)
                tvScoreIcon.text = context.getString(R.string.icon_dormant_seed)
            }
            Profile.ScoreTheme.NOT_EVALUATED -> {
                rootLayout.setBackgroundResource(R.drawable.bg_not_evaluated)
                tvScoreIcon.text = context.getString(R.string.icon_dormant_seed)
            }
        }
    }

    fun applyOhaengTheme(
        context: Context,
        containers: List<LinearLayout>,
        theme: Profile.ScoreTheme,
        values: List<Int>
    ) {
        containers.forEachIndexed { index, container ->
            if (index < values.size) {
                val value = values[index]
                val isLacking = value == 0

                val backgroundResource = when (theme) {
                    Profile.ScoreTheme.SUNNY_SPRING -> {
                        if (isLacking) R.drawable.bg_ohaeng_lacking_sunny
                        else R.drawable.bg_ohaeng_normal_sunny
                    }
                    Profile.ScoreTheme.WARM_SPRING -> {
                        if (isLacking) R.drawable.bg_ohaeng_lacking_warm
                        else R.drawable.bg_ohaeng_normal_warm
                    }
                    Profile.ScoreTheme.CLOUDY_SPRING -> {
                        if (isLacking) R.drawable.bg_ohaeng_lacking_cloudy
                        else R.drawable.bg_ohaeng_normal_cloudy
                    }
                    Profile.ScoreTheme.RAINY_SPRING -> {
                        if (isLacking) R.drawable.bg_ohaeng_lacking_rainy
                        else R.drawable.bg_ohaeng_normal_rainy
                    }
                    Profile.ScoreTheme.COLD_SPRING -> {
                        if (isLacking) R.drawable.bg_ohaeng_lacking_cold
                        else R.drawable.bg_ohaeng_normal_cold
                    }
                    Profile.ScoreTheme.NOT_EVALUATED -> {
                        if (isLacking) R.drawable.bg_ohaeng_lacking_neutral
                        else R.drawable.bg_ohaeng_normal_neutral
                    }
                }
                container.setBackgroundResource(backgroundResource)
            }
        }
    }
}