// model/data/source/EvaluatorData.kt
package com.ssc.namespring.model.data.source

import com.google.gson.annotations.SerializedName

data class PersonalityEvaluatorStrings(
    @SerializedName("social_index")
    val socialIndex: Int,

    val thresholds: Map<String, Int>
)

data class CareerEvaluatorStrings(
    @SerializedName("success_factors")
    val successFactors: Map<String, String>,

    @SerializedName("work_style_keywords")
    val workStyleKeywords: Map<String, String>,

    @SerializedName("personality_keywords")
    val personalityKeywords: Map<String, String>,

    val thresholds: Map<String, Int>,

    @SerializedName("magic_numbers")
    val magicNumbers: Map<String, Int>
)

data class FortuneEvaluatorStrings(
    @SerializedName("example_names")
    val exampleNames: List<String>,

    @SerializedName("life_periods")
    val lifePeriods: Map<String, String>,

    @SerializedName("period_names")
    val periodNames: Map<String, String>,

    @SerializedName("lucky_element_format")
    val luckyElementFormat: String,

    @SerializedName("energy_format")
    val energyFormat: String,

    @SerializedName("challenge_format")
    val challengeFormat: String,

    @SerializedName("business_opportunity")
    val businessOpportunity: String,

    @SerializedName("period_influence_format")
    val periodInfluenceFormat: String,

    val thresholds: Map<String, Int>,

    @SerializedName("element_cycle")
    val elementCycle: Map<String, String>
)

data class LifePeriods(
    @SerializedName("life_period_names")
    val lifePeriodNames: Map<String, String>,

    @SerializedName("life_period_mapping")
    val lifePeriodMapping: Map<String, String>
)

data class PersonalityTraits(
    @SerializedName("social_styles")
    val socialStyles: Map<String, String>,

    @SerializedName("work_styles")
    val workStyles: Map<String, String>,

    @SerializedName("weakness_transformations")
    val weaknessTransformations: Map<String, String>
)

data class BusinessLuckStrokes(
    @SerializedName("business_luck_strokes")
    val businessLuckStrokes: List<Int>,

    @SerializedName("leadership_strokes")
    val leadershipStrokes: List<Int>,

    @SerializedName("unsuitable_for_stability")
    val unsuitableForStability: List<Int>,

    @SerializedName("unsuitable_for_adventure")
    val unsuitableForAdventure: List<Int>,

    @SerializedName("business_evaluations")
    val businessEvaluations: Map<String, String>,

    @SerializedName("leadership_evaluations")
    val leadershipEvaluations: Map<String, String>
)

data class CareerFields(
    @SerializedName("avoidance_strokes")
    val avoidanceStrokes: AvoidanceStrokes
)

data class AvoidanceStrokes(
    @SerializedName("high_risk")
    val highRisk: List<Int>,

    @SerializedName("avoid_message")
    val avoidMessage: String,

    @SerializedName("stability_unsuitable")
    val stabilityUnsuitable: List<Int>,

    @SerializedName("stability_message")
    val stabilityMessage: String
)
