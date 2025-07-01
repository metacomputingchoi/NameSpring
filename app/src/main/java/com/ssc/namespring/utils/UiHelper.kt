// utils/UiHelper.kt
package com.ssc.namespring.utils

import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.data.Theme

/**
 * UI 개발을 위한 헬퍼 클래스
 * 자주 사용되는 UI 관련 유틸리티 메서드 제공
 */
object UiHelper {

    /**
     * 이름봄 점수에 따른 새싹 아이콘 텍스트
     */
    fun getSproutIcon(score: Int): String {
        return when (score) {
            in 80..100 -> "🌸"  // 만개
            in 60..79 -> "🌳"   // 성장
            in 40..59 -> "🌿"   // 새싹
            in 20..39 -> "🌱"   // 발아
            else -> "🌰"        // 씨앗
        }
    }

    /**
     * GeneratedName의 점수 계산 (0-100)
     */
    fun getNamebomScore(name: GeneratedName): Int {
        val analysisInfo = name.analysisInfo ?: return 0
        val totalScore = analysisInfo.totalScore

        // totalScore는 일반적으로 0-160 범위이므로 100점 만점으로 변환
        return (totalScore * 100 / 160).coerceIn(0, 100)
    }

    /**
     * 이름의 주요 특징 추출 (최대 3개)
     */
    fun extractNameFeatures(name: GeneratedName): List<String> {
        val features = mutableListOf<String>()
        val analysisInfo = name.analysisInfo ?: return features

        // 음양 균형
        if (analysisInfo.eumYangInfo.isBalanced) {
            features.add("음양균형")
        }

        // 오행 조화
        if (analysisInfo.ohaengInfo.overallHarmony.contains("조화")) {
            features.add("오행조화")
        }

        // 사격 점수
        val sagyeokScore = analysisInfo.scoreBreakdown["사격점수"] ?: 0
        when {
            sagyeokScore >= 100 -> features.add("최상급 사격")
            sagyeokScore >= 75 -> features.add("우수한 사격")
        }

        // 발음
        if (analysisInfo.filteringSteps.any {
                it.filterName.contains("발음") && it.passed
            }) {
            features.add("자연스러운 발음")
        }

        return features.take(3)
    }

    /**
     * 테마별 색상 코드 (임시)
     * 실제 UI 개발 시 colors.xml로 이동
     */
    fun getThemeColors(theme: Theme): ThemeColors {
        return when (theme.name) {
            "화창한 봄" -> ThemeColors(
                primary = "#FFB6C1",    // 연분홍
                secondary = "#98FB98",   // 연두
                background = "#F0F8FF",  // 하늘색
                accent = "#FF69B4"       // 진분홍
            )
            "따뜻한 봄" -> ThemeColors(
                primary = "#FFDAB9",     // 살구색
                secondary = "#FFFACD",   // 연노랑
                background = "#FFF8DC",  // 크림색
                accent = "#FFB347"       // 주황
            )
            "흐린 봄" -> ThemeColors(
                primary = "#D3D3D3",     // 연회색
                secondary = "#E6E6FA",   // 연보라
                background = "#F5F5F5",  // 미색
                accent = "#9370DB"       // 보라
            )
            "비내리는 봄" -> ThemeColors(
                primary = "#B0C4DE",     // 연청색
                secondary = "#DDA0DD",   // 연자주
                background = "#E0E0E0",  // 회색
                accent = "#4682B4"       // 청색
            )
            else -> ThemeColors(
                primary = "#87CEEB",     // 하늘색
                secondary = "#DCDCDC",   // 은색
                background = "#F0FFFF",  // 연하늘
                accent = "#4169E1"       // 파랑
            )
        }
    }

    data class ThemeColors(
        val primary: String,
        val secondary: String,
        val background: String,
        val accent: String
    )
}