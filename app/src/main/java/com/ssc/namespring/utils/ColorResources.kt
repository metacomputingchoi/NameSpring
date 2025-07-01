// utils/ColorResources.kt
package com.ssc.namespring.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.ssc.namespring.R
import com.ssc.namespring.model.data.Theme

/**
 * 색상 리소스 유틸리티
 * XML에 정의된 색상을 코드에서 쉽게 사용하기 위한 헬퍼
 */
object ColorResources {

    fun getThemeColors(context: Context, theme: Theme): ThemeColorSet {
        return when (theme.name) {
            "화창한 봄" -> ThemeColorSet(
                primary = getColor(context, R.color.sunny_spring_primary),
                background = getColor(context, R.color.sunny_spring_bg),
                accent = getColor(context, R.color.sunny_spring_accent),
                textPrimary = getColor(context, android.R.color.black),
                textSecondary = getColor(context, android.R.color.darker_gray)
            )
            "따뜻한 봄" -> ThemeColorSet(
                primary = getColor(context, R.color.warm_spring_primary),
                background = getColor(context, R.color.warm_spring_bg),
                accent = getColor(context, R.color.warm_spring_accent),
                textPrimary = getColor(context, android.R.color.black),
                textSecondary = getColor(context, android.R.color.darker_gray)
            )
            "흐린 봄" -> ThemeColorSet(
                primary = getColor(context, R.color.cloudy_spring_primary),
                background = getColor(context, R.color.cloudy_spring_bg),
                accent = getColor(context, R.color.cloudy_spring_accent),
                textPrimary = getColor(context, android.R.color.black),
                textSecondary = getColor(context, android.R.color.darker_gray)
            )
            "비내리는 봄" -> ThemeColorSet(
                primary = getColor(context, R.color.rainy_spring_primary),
                background = getColor(context, R.color.rainy_spring_bg),
                accent = getColor(context, R.color.rainy_spring_accent),
                textPrimary = getColor(context, android.R.color.white),
                textSecondary = getColor(context, android.R.color.holo_blue_light)
            )
            "쌀쌀한 봄" -> ThemeColorSet(
                primary = getColor(context, R.color.cold_spring_primary),
                background = getColor(context, R.color.cold_spring_bg),
                accent = getColor(context, R.color.cold_spring_accent),
                textPrimary = getColor(context, android.R.color.white),
                textSecondary = getColor(context, android.R.color.holo_blue_light)
            )
            else -> ThemeColorSet(
                primary = getColor(context, R.color.primary),
                background = getColor(context, R.color.background),
                accent = getColor(context, R.color.accent),
                textPrimary = getColor(context, R.color.text_primary),
                textSecondary = getColor(context, R.color.text_secondary)
            )
        }
    }

    fun getSproutColor(context: Context, state: com.ssc.namespring.model.data.SproutState): Int {
        return when (state) {
            com.ssc.namespring.model.data.SproutState.DORMANT -> getColor(context, R.color.dormant_gray)
            com.ssc.namespring.model.data.SproutState.SEED -> getColor(context, R.color.seed_brown)
            com.ssc.namespring.model.data.SproutState.SPROUTING -> getColor(context, R.color.sprout_green)
            com.ssc.namespring.model.data.SproutState.GROWING -> getColor(context, R.color.leaf_green)
            com.ssc.namespring.model.data.SproutState.BLOOMING -> getColor(context, R.color.flower_pink)
        }
    }

    private fun getColor(context: Context, colorResId: Int): Int {
        return ContextCompat.getColor(context, colorResId)
    }

    data class ThemeColorSet(
        val primary: Int,
        val background: Int,
        val accent: Int,
        val textPrimary: Int,
        val textSecondary: Int
    )
}