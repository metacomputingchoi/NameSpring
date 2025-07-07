// model/domain/service/report/generators/DetailedAnalysisSectionGenerator.kt
package com.ssc.namespring.model.domain.service.report.generators

import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.report.interfaces.IReportSectionGenerator
import com.ssc.namespring.model.domain.service.report.utils.RecommendationGenerator
import com.ssc.namespring.model.domain.service.report.utils.CompatibilityCalculator

class DetailedAnalysisSectionGenerator(
    private val recommendationGenerator: RecommendationGenerator = RecommendationGenerator(),
    private val compatibilityCalculator: CompatibilityCalculator = CompatibilityCalculator()
) : IReportSectionGenerator {

    override fun getSectionName(): String = "detailedAnalysis"

    override fun generateSection(profile: Profile): Map<String, Any> {
        val analysis = mutableMapOf<String, Any>()

        // Character meanings
        profile.givenName?.charInfos?.let { charInfos ->
            analysis["characterMeanings"] = generateCharacterMeanings(charInfos)
        }

        // Recommendations
        analysis["recommendations"] = recommendationGenerator.generate(profile)

        // Compatibility analysis
        analysis["compatibility"] = mapOf(
            "withSurname" to compatibilityCalculator.calculateNameCompatibility(profile),
            "withBirthDate" to compatibilityCalculator.calculateBirthDateCompatibility(profile)
        )

        return analysis
    }

    private fun generateCharacterMeanings(charInfos: List<com.ssc.namespring.model.domain.entity.CharInfo>): List<Map<String, Any>> {
        return charInfos.mapIndexed { index, charInfo ->
            mapOf(
                "position" to (index + 1),
                "korean" to charInfo.korean,
                "hanja" to charInfo.hanja,
                "meaning" to (charInfo.meaning ?: "의미 정보 없음"),
                "strokes" to charInfo.strokes,
                "ohaeng" to (charInfo.ohaeng ?: ""),
                "eumyang" to if (charInfo.eumyang == 1) "양(陽)" else "음(陰)"
            )
        }
    }
}