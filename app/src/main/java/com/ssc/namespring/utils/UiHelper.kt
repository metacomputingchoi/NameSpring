// utils/UiHelper.kt
package com.ssc.namespring.utils

import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.data.Theme
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.data.Favorite
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
     * 점수별 축하 메시지 생성
     */
    fun getCelebrationMessage(score: Int): String {
        val celebration = JsonLoader.getCelebrationMessage(score)
        return celebration?.messages?.randomOrNull() ?: ""
    }

    /**
     * 프로필별 환영 메시지 생성
     */
    fun getWelcomeMessage(profile: Profile): String {
        val hour = LocalDateTime.now().hour
        val timeMessage = JsonLoader.getTimeBasedMessage(hour) ?: ""

        return "${profile.profileName}님, $timeMessage"
    }

    /**
     * 이름 생성 결과 요약 메시지
     */
    fun getGenerationSummaryMessage(count: Int, topScore: Int): String {
        return when {
            count == 0 -> "조건에 맞는 이름이 없습니다 😢"
            topScore >= 90 -> "최고의 이름들을 찾았습니다! 🎉"
            topScore >= 80 -> "좋은 이름들을 찾았습니다! ✨"
            topScore >= 70 -> "괜찮은 이름들이 있네요 🌱"
            else -> "더 나은 이름을 찾아볼까요? 💪"
        }
    }

    /**
     * 즐겨찾기 통계 메시지 생성
     */
    fun getFavoriteStatisticsMessage(favorites: List<Favorite>): String {
        if (favorites.isEmpty()) return "아직 즐겨찾기가 없습니다"

        val avgScore = favorites.map { it.getNamebomScore() }.average().toInt()
        val topScore = favorites.maxOfOrNull { it.getNamebomScore() } ?: 0

        return "즐겨찾기 ${favorites.size}개 (평균 ${avgScore}점, 최고 ${topScore}점)"
    }

    /**
     * 날짜 포맷팅
     */
    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"))
    }

    /**
     * 날짜 간단 포맷팅
     */
    fun formatDateSimple(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
    }

    /**
     * 프로그레스 바 생성
     */
    fun createProgressBar(value: Int, max: Int = 100, length: Int = 20): String {
        val filled = (value * length / max).coerceIn(0, length)
        val empty = length - filled

        return buildString {
            append("[")
            repeat(filled) { append("█") }
            repeat(empty) { append("░") }
            append("] ${value}/${max}")
        }
    }

    /**
     * 미니 프로그레스 바 생성
     */
    fun createMiniProgressBar(value: Int, max: Int = 100): String {
        val filled = (value * 10 / max).coerceIn(0, 10)
        return "▓".repeat(filled) + "░".repeat(10 - filled)
    }

    /**
     * 점수별 색상 코드 (추후 실제 색상 리소스로 대체)
     */
    fun getScoreColor(score: Int): String {
        return when {
            score >= 90 -> "#FF6B6B"  // 빨강 (최고)
            score >= 80 -> "#4ECDC4"  // 청록 (우수)
            score >= 70 -> "#45B7D1"  // 파랑 (양호)
            score >= 60 -> "#96CEB4"  // 녹색 (보통)
            else -> "#FECA57"         // 노랑 (주의)
        }
    }

    /**
     * 등급별 메달 아이콘
     */
    fun getRankMedal(rank: Int): String {
        return when (rank) {
            1 -> "🥇"
            2 -> "🥈"
            3 -> "🥉"
            else -> "${rank}위"
        }
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