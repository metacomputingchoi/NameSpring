// model/domain/service/report/utils/RecommendationGenerator.kt
package com.ssc.namespring.model.domain.service.report.utils

import com.ssc.namespring.model.domain.entity.Profile

class RecommendationGenerator {

    fun generate(profile: Profile): List<String> {
        val recommendations = mutableListOf<String>()

        addScoreBasedRecommendations(profile, recommendations)
        addOhaengBasedRecommendations(profile, recommendations)

        return recommendations
    }

    private fun addScoreBasedRecommendations(profile: Profile, recommendations: MutableList<String>) {
        when (profile.nameBomScore) {
            in 0..59 -> recommendations.add("이름 재검토를 권장합니다")
            in 60..79 -> recommendations.add("보완이 필요한 부분을 개선하시면 좋습니다")
            in 80..100 -> recommendations.add("좋은 이름입니다. 현재 상태를 유지하세요")
        }
    }

    private fun addOhaengBasedRecommendations(profile: Profile, recommendations: MutableList<String>) {
        profile.sajuInfo?.missingElements?.forEach { element ->
            recommendations.add("부족한 $element 기운을 보완하는 것이 좋습니다")
        }
    }
}