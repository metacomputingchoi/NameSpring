// model/domain/name/value/DetailedNameReport.kt
package com.ssc.namespring.model.domain.name.value

data class DetailedNameReport(
    val basicInfo: BasicInfo,
    val sajuDetail: SajuDetail,
    val strokeDetail: StrokeDetail,
    val elementDetail: ElementDetail,
    val yinYangDetail: YinYangDetail,
    val characterAnalysis: CharacterAnalysis,
    val fortunePrediction: FortunePrediction,
    val personalityAnalysis: PersonalityAnalysis,
    val careerGuidance: CareerGuidance,
    val comprehensiveAdvice: ComprehensiveAdvice
)

data class BasicInfo(
    val fullName: String,
    val fullHanja: String,
    val totalScore: Int,
    val grade: String,
    val scoreBreakdown: Map<String, Pair<Int, Int>>
)

data class SajuDetail(
    val fourPillars: Map<String, String>,
    val elementDistribution: Map<String, Int>,
    val dominantElement: String,
    val lackingElements: List<String>,
    val elementBalance: String,
    val seasonalInfluence: String,
    val dayMasterAnalysis: String
)

data class StrokeDetail(
    val strokeNumbers: Map<String, Int>,
    val luckAnalysis: Map<String, String>,
    val numerologyMeaning: Map<String, String>,
    val lifePathInfluence: String
)

data class ElementDetail(
    val nameElements: List<String>,
    val relationshipMap: List<ElementRelation>,
    val cycleAnalysis: String,
    val strengthAnalysis: Map<String, String>
)

data class ElementRelation(
    val from: String,
    val to: String,
    val type: String,
    val influence: String
)

data class YinYangDetail(
    val pattern: String,
    val distribution: Map<String, Int>,
    val balanceScore: Int,
    val flowAnalysis: String,
    val energyType: String
)

data class CharacterAnalysis(
    val hanjaDetails: List<HanjaDetail>,
    val combinedMeaning: String,
    val symbolicInterpretation: String,
    val culturalSignificance: String
)

data class HanjaDetail(
    val character: String,
    val meaning: String,
    val origin: String,
    val components: List<String>,
    val relatedCharacters: List<String>
)

data class FortunePrediction(
    val overallFortune: String,
    val lifePeriods: Map<String, String>,
    val luckyElements: List<String>,
    val challengePeriods: List<String>,
    val opportunityAreas: List<String>
)

data class PersonalityAnalysis(
    val coreTraits: List<String>,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val emotionalTendency: String,
    val socialStyle: String,
    val leadershipPotential: String
)

data class CareerGuidance(
    val suitableFields: List<String>,
    val avoidFields: List<String>,
    val workStyle: String,
    val successFactors: List<String>,
    val businessLuck: String
)

data class ComprehensiveAdvice(
    val summary: String,
    val keyRecommendations: List<String>,
    val cautionPoints: List<String>,
    val developmentAreas: List<String>,
    val luckyColors: List<String>,
    val luckyNumbers: List<Int>,
    val compatibleNames: List<String>
)