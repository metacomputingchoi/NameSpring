// model/data/json/NewJsonDataClasses.kt
package com.ssc.namespring.model.data.json

import com.google.gson.annotations.SerializedName

// name_evaluation_messages.json 관련 클래스들
data class NameEvaluationMessages(
    @SerializedName("score_range_messages")
    val scoreRangeMessages: Map<String, ScoreRangeMessage>,

    @SerializedName("category_excellence_messages")
    val categoryExcellenceMessages: Map<String, CategoryMessages>,

    @SerializedName("special_combination_messages")
    val specialCombinationMessages: Map<String, String>
)

data class ScoreRangeMessage(
    val title: String,
    val description: String,
    val emoji: String,
    val recommendations: List<String>
) {
    val scoreRange: IntRange
        get() = when {
            title.contains("천부적인 명품") -> 95..100
            title.contains("최상급 명품") -> 90..94
            title.contains("우수한") -> 85..89
            title.contains("양호한") -> 80..84
            title.contains("무난한") -> 75..79
            title.contains("보통") -> 70..74
            title.contains("주의가 필요한") -> 65..69
            title.contains("재고가 필요한") -> 60..64
            else -> 0..59
        }
}

data class CategoryMessages(
    val excellent: String,
    val good: String,
    val average: String,
    val poor: String
)

// celebration_messages.json 관련 클래스들
data class CelebrationMessages(
    @SerializedName("name_found_celebrations")
    val nameFoundCelebrations: Map<String, CelebrationMessage>,

    @SerializedName("milestone_messages")
    val milestoneMessages: Map<String, String>,

    @SerializedName("encouragement_messages")
    val encouragementMessages: Map<String, List<String>>,

    @SerializedName("seasonal_messages")
    val seasonalMessages: Map<String, SeasonalMessage>,

    @SerializedName("time_based_messages")
    val timeBasedMessages: Map<String, String>
)

data class CelebrationMessage(
    val messages: List<String>,
    val animations: List<String>
)

data class SeasonalMessage(
    val message: String,

    @SerializedName("lucky_elements")
    val luckyElements: List<String>,

    @SerializedName("special_bonus")
    val specialBonus: String
)

// naming_tips.json 관련 클래스들
data class NamingTips(
    @SerializedName("general_tips")
    val generalTips: GeneralTips,

    @SerializedName("saju_based_tips")
    val sajuBasedTips: Map<String, SajuTips>,

    @SerializedName("stroke_number_tips")
    val strokeNumberTips: StrokeNumberTips,

    @SerializedName("pronunciation_tips")
    val pronunciationTips: PronunciationTips,

    @SerializedName("meaning_combination_tips")
    val meaningCombinationTips: MeaningTips,

    @SerializedName("modern_naming_trends")
    val modernNamingTrends: ModernNamingTrends,

    @SerializedName("special_considerations")
    val specialConsiderations: Map<String, SpecialConsideration>,

    @SerializedName("ai_era_naming")
    val aiEraNaming: AiEraNaming,

    @SerializedName("consultation_advice")
    val consultationAdvice: ConsultationAdvice
)

data class GeneralTips(
    val title: String,
    val tips: List<String>
)

data class SajuTips(
    val description: String,
    val tips: List<String>?,
    @SerializedName("recommended_hanja")
    val recommendedHanja: List<String>?
)

data class StrokeNumberTips(
    @SerializedName("good_numbers")
    val goodNumbers: GoodNumbers,

    @SerializedName("avoid_numbers")
    val avoidNumbers: AvoidNumbers,

    @SerializedName("special_considerations")
    val specialConsiderations: Map<String, String>
)

data class GoodNumbers(
    val description: String,
    val numbers: Map<String, List<Int>>,
    val tip: String
)

data class AvoidNumbers(
    val description: String,
    val numbers: Map<String, List<Int>>,
    val tip: String
)

data class PronunciationTips(
    val title: String,
    val tips: List<String>,
    val examples: Map<String, List<String>>
)

data class ModernNamingTrends(
    val title: String,
    val trends: List<String>,
    @SerializedName("popular_themes")
    val popularThemes: Map<String, List<String>>
)

data class SpecialConsideration(
    val title: String,
    val tips: List<String>,
    val examples: List<String>? = null
)

data class Tips(
    val title: String,
    val tips: List<String>?
)

data class MeaningTips(
    val title: String,

    @SerializedName("good_combinations")
    val goodCombinations: List<String>,

    @SerializedName("avoid_combinations")
    val avoidCombinations: List<String>
)

data class AiEraNaming(
    val title: String,
    val considerations: List<String>
)

data class ConsultationAdvice(
    val title: String,
    val checklist: List<String>
)

// user_guide_strings.json 관련 클래스들
data class UserGuideStrings(
    @SerializedName("onboarding_guide")
    val onboardingGuide: Map<String, GuideSection>,

    @SerializedName("feature_guides")
    val featureGuides: Map<String, FeatureGuide>,

    @SerializedName("interpretation_guide")
    val interpretationGuide: Map<String, Any>,

    @SerializedName("error_messages")
    val errorMessages: Map<String, Map<String, String>>,

    @SerializedName("help_tooltips")
    val helpTooltips: Map<String, String>
)

data class GuideSection(
    val title: String,
    val messages: List<String>? = null,
    val steps: List<String>? = null
)

data class FeatureGuide(
    val title: String,
    val description: String,
    val tips: List<String>
)