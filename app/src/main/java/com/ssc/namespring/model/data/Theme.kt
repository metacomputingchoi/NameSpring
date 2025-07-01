// model/data/Theme.kt
package com.ssc.namespring.model.data

import androidx.annotation.ColorRes
import com.ssc.namespring.R

/**
 * 이름봄 점수에 따른 테마 정보
 */
data class Theme(
    val name: String,
    val description: String,
    val scoreRange: IntRange,
    @ColorRes val primaryColor: Int,
    @ColorRes val backgroundColor: Int,
    val backgroundGradientEndColor: Int? = null,  // 그라데이션 끝 색상
    val sproutState: SproutState,
    val weatherType: WeatherType,
    val themeEmoji: String = ""  // 테마를 나타내는 이모지
) {
    companion object {
        val SUNNY_SPRING = Theme(
            name = "화창한 봄",
            description = "맑은 하늘, 활짝 핀 꽃",
            scoreRange = 80..100,
            primaryColor = R.color.sunny_spring_primary,
            backgroundColor = R.color.sunny_spring_bg,
            backgroundGradientEndColor = R.color.sunny_spring_accent,
            sproutState = SproutState.BLOOMING,
            weatherType = WeatherType.SUNNY,
            themeEmoji = "🌸"
        )

        val WARM_SPRING = Theme(
            name = "따뜻한 봄",
            description = "부드러운 햇살",
            scoreRange = 60..79,
            primaryColor = R.color.warm_spring_primary,
            backgroundColor = R.color.warm_spring_bg,
            backgroundGradientEndColor = R.color.warm_spring_accent,
            sproutState = SproutState.GROWING,
            weatherType = WeatherType.WARM,
            themeEmoji = "🌷"
        )

        val CLOUDY_SPRING = Theme(
            name = "흐린 봄",
            description = "구름 낀 하늘",
            scoreRange = 40..59,
            primaryColor = R.color.cloudy_spring_primary,
            backgroundColor = R.color.cloudy_spring_bg,
            backgroundGradientEndColor = R.color.cloudy_spring_accent,
            sproutState = SproutState.SPROUTING,
            weatherType = WeatherType.CLOUDY,
            themeEmoji = "🌫️"
        )

        val RAINY_SPRING = Theme(
            name = "비내리는 봄",
            description = "봄비",
            scoreRange = 20..39,
            primaryColor = R.color.rainy_spring_primary,
            backgroundColor = R.color.rainy_spring_bg,
            backgroundGradientEndColor = R.color.rainy_spring_accent,
            sproutState = SproutState.SEED,
            weatherType = WeatherType.RAINY,
            themeEmoji = "🌧️"
        )

        val COLD_SPRING = Theme(
            name = "쌀쌀한 봄",
            description = "꽃샘추위",
            scoreRange = 0..19,
            primaryColor = R.color.cold_spring_primary,
            backgroundColor = R.color.cold_spring_bg,
            backgroundGradientEndColor = R.color.cold_spring_accent,
            sproutState = SproutState.DORMANT,
            weatherType = WeatherType.COLD,
            themeEmoji = "❄️"
        )

        fun getAll(): List<Theme> = listOf(
            SUNNY_SPRING, WARM_SPRING, CLOUDY_SPRING, RAINY_SPRING, COLD_SPRING
        )
    }
}

enum class SproutState {
    DORMANT,    // 휴면 (씨앗)
    SEED,       // 씨앗
    SPROUTING,  // 새싹
    GROWING,    // 성장중
    BLOOMING    // 개화
}

enum class WeatherType {
    SUNNY,   // 화창
    WARM,    // 따뜻
    CLOUDY,  // 흐림
    RAINY,   // 비
    COLD     // 추움
}