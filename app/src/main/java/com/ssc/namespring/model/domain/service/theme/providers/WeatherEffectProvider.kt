// model/domain/service/theme/providers/WeatherEffectProvider.kt
package com.ssc.namespring.model.domain.service.theme.providers

import com.ssc.namespring.model.domain.entity.WeatherType

class WeatherEffectProvider {

    fun getWeatherEffectText(weatherType: WeatherType): String? {
        return when (weatherType) {
            WeatherType.SUNNY -> null
            WeatherType.WARM -> "✨"
            WeatherType.CLOUDY -> "☁️"
            WeatherType.RAINY -> "🌧️"
            WeatherType.COLD -> "❄️"
        }
    }

    fun getWeatherEffectDescription(weatherType: WeatherType): String {
        return when (weatherType) {
            WeatherType.SUNNY -> "맑은 하늘"
            WeatherType.WARM -> "따뜻한 햇살이 비춥니다"
            WeatherType.CLOUDY -> "구름이 끼어있습니다"
            WeatherType.RAINY -> "봄비가 내립니다"
            WeatherType.COLD -> "꽃샘추위가 있습니다"
        }
    }
}
