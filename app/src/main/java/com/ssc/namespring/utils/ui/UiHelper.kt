// utils/ui/UiHelper.kt
package com.ssc.namespring.utils.ui

import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.domain.entity.Theme

object UiHelper {

    fun getSproutIcon(score: Int): String {
        return when (score) {
            in 80..100 -> "🌸"
            in 60..79 -> "🌳"
            in 40..59 -> "🌿"
            in 20..39 -> "🌱"
            else -> "🌰"
        }
    }

    fun getNamebomScore(name: GeneratedName): Int {
        val analysisInfo = name.analysisInfo ?: return 0
        val totalScore = analysisInfo.totalScore
        return (totalScore * 100 / 160).coerceIn(0, 100)
    }

    fun extractNameFeatures(name: GeneratedName): List<String> {
        val features = mutableListOf<String>()
        val analysisInfo = name.analysisInfo ?: return features

        if (analysisInfo.eumYangInfo.isBalanced) {
            features.add("음양균형")
        }

        if (analysisInfo.ohaengInfo.overallHarmony.contains("조화")) {
            features.add("오행조화")
        }

        val sagyeokScore = analysisInfo.scoreBreakdown["사격점수"] ?: 0
        when {
            sagyeokScore >= 100 -> features.add("최상급 사격")
            sagyeokScore >= 75 -> features.add("우수한 사격")
        }

        if (analysisInfo.filteringSteps.any {
                it.filterName.contains("발음") && it.passed
            }) {
            features.add("자연스러운 발음")
        }

        return features.take(3)
    }

    fun getThemeColors(theme: Theme): ThemeColorsUI {
        return when (theme.name) {
            "화창한 봄" -> ThemeColorsUI(
                primary = "#FFB6C1",
                secondary = "#98FB98",
                background = "#F0F8FF",
                accent = "#FF69B4"
            )
            "따뜻한 봄" -> ThemeColorsUI(
                primary = "#FFDAB9",
                secondary = "#FFFACD",
                background = "#FFF8DC",
                accent = "#FFB347"
            )
            "흐린 봄" -> ThemeColorsUI(
                primary = "#D3D3D3",
                secondary = "#E6E6FA",
                background = "#F5F5F5",
                accent = "#9370DB"
            )
            "비내리는 봄" -> ThemeColorsUI(
                primary = "#B0C4DE",
                secondary = "#DDA0DD",
                background = "#E0E0E0",
                accent = "#4682B4"
            )
            else -> ThemeColorsUI(
                primary = "#87CEEB",
                secondary = "#DCDCDC",
                background = "#F0FFFF",
                accent = "#4169E1"
            )
        }
    }

    data class ThemeColorsUI(
        val primary: String,
        val secondary: String,
        val background: String,
        val accent: String
    )
}
