// model/domain/service/theme/providers/ThemeMessageProvider.kt
package com.ssc.namespring.model.domain.service.theme.providers

import com.ssc.namespring.model.domain.entity.Theme

class ThemeMessageProvider {

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
}
