// model/ThemeModel.kt
package com.ssc.namespring.model

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.ssc.namespring.R
import com.ssc.namespring.model.data.Theme
import com.ssc.namespring.model.data.SproutState
import com.ssc.namespring.model.data.WeatherType

/**
 * 테마 관리 비즈니스 로직
 * 이름봄 점수에 따른 UI 테마 결정
 */
class ThemeModel(private val context: Context) {

    /**
     * 점수에 따른 테마 반환
     */
    fun getThemeByScore(score: Int): Theme {
        return Theme.getAll().find { score in it.scoreRange } ?: Theme.COLD_SPRING
    }

    /**
     * 테마 전환 애니메이션 시간
     */
    fun getThemeTransitionDuration(): Long = 600L

    /**
     * 새싹 성장 애니메이션 시간
     */
    fun getSproutAnimationDuration(): Long = 1000L

    /**
     * 테마별 색상 팔레트
     */
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

    /**
     * 점수 변화에 따른 테마 변경 필요 여부
     */
    fun shouldChangeTheme(oldScore: Int, newScore: Int): Boolean {
        val oldTheme = getThemeByScore(oldScore)
        val newTheme = getThemeByScore(newScore)
        return oldTheme != newTheme
    }

    /**
     * 새싹 아이콘 텍스트 (임시 - 추후 실제 아이콘으로 교체)
     * 유니코드 이모지를 사용하여 표현
     */
    fun getSproutIconText(state: SproutState): String {
        return when (state) {
            SproutState.DORMANT -> "🌰"    // 도토리 (휴면)
            SproutState.SEED -> "🌱"        // 새싹 (씨앗)
            SproutState.SPROUTING -> "🌿"   // 허브 (발아)
            SproutState.GROWING -> "🌳"     // 나무 (성장)
            SproutState.BLOOMING -> "🌸"    // 벚꽃 (개화)
        }
    }

    /**
     * 새싹 아이콘 리소스 ID
     */
    fun getSproutIconResource(state: SproutState): Int {
        return when (state) {
            SproutState.DORMANT -> R.drawable.ic_dormant_seed
            SproutState.SEED -> R.drawable.ic_seed
            SproutState.SPROUTING -> R.drawable.ic_sprout
            SproutState.GROWING -> R.drawable.ic_sprout_bloom
            SproutState.BLOOMING -> R.drawable.ic_flower_full
        }
    }

    /**
     * 새싹 아이콘 색상 (아이콘 대신 색상으로 구분)
     */
    fun getSproutIconColor(state: SproutState): Int {
        return when (state) {
            SproutState.DORMANT -> R.color.dormant_gray
            SproutState.SEED -> R.color.seed_brown
            SproutState.SPROUTING -> R.color.sprout_green
            SproutState.GROWING -> R.color.leaf_green
            SproutState.BLOOMING -> R.color.flower_pink
        }
    }

    /**
     * 날씨 효과 텍스트 (애니메이션 대신 텍스트로 표현)
     */
    fun getWeatherEffectText(weatherType: WeatherType): String? {
        return when (weatherType) {
            WeatherType.SUNNY -> null       // 특별한 효과 없음
            WeatherType.WARM -> "✨"        // 반짝임
            WeatherType.CLOUDY -> "☁️"      // 구름
            WeatherType.RAINY -> "🌧️"       // 비
            WeatherType.COLD -> "❄️"        // 눈
        }
    }

    /**
     * 날씨 효과 설명 (시각적 효과 대신 텍스트 설명)
     */
    fun getWeatherEffectDescription(weatherType: WeatherType): String {
        return when (weatherType) {
            WeatherType.SUNNY -> "맑은 하늘"
            WeatherType.WARM -> "따뜻한 햇살이 비춥니다"
            WeatherType.CLOUDY -> "구름이 끼어있습니다"
            WeatherType.RAINY -> "봄비가 내립니다"
            WeatherType.COLD -> "꽃샘추위가 있습니다"
        }
    }

    /**
     * 테마별 추천 메시지
     */
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

    /**
     * 새싹 성장 단계 설명
     */
    fun getSproutStateDescription(state: SproutState): String {
        return when (state) {
            SproutState.DORMANT -> "씨앗이 땅속에서 잠들어 있어요"
            SproutState.SEED -> "씨앗이 심어졌어요"
            SproutState.SPROUTING -> "새싹이 돋아나고 있어요"
            SproutState.GROWING -> "무럭무럭 자라고 있어요"
            SproutState.BLOOMING -> "아름다운 꽃이 피었어요"
        }
    }

    /**
     * 테마 배경 그라데이션 색상 (이미지 대신 그라데이션 사용)
     */
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