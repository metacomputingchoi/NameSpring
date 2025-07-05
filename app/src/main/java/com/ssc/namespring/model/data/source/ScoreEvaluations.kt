// model/data/source/ScoreEvaluations.kt
package com.ssc.namespring.model.data.source

import com.google.gson.annotations.SerializedName

data class ScoreEvaluations(
    @SerializedName("score_thresholds")
    val scoreThresholds: ScoreThresholds,

    @SerializedName("overall_evaluations")
    val overallEvaluations: Map<String, String>,

    @SerializedName("grade_assessments")
    val gradeAssessments: Map<String, String>,

    @SerializedName("recommendations")
    val recommendations: Map<String, String>,

    @SerializedName("strength_messages")
    val strengthMessages: Map<String, String>,

    @SerializedName("weakness_messages")
    val weaknessMessages: Map<String, String>,

    @SerializedName("detailed_recommendations")
    val detailedRecommendations: Map<String, String>
)

data class ScoreThresholds(
    @SerializedName("grade_A")
    val gradeA: Int,

    @SerializedName("grade_B")
    val gradeB: Int,

    @SerializedName("grade_C")
    val gradeC: Int,

    @SerializedName("score_high_threshold_1")
    val scoreHighThreshold1: Int,

    @SerializedName("score_high_threshold_2")
    val scoreHighThreshold2: Int,

    @SerializedName("score_high_threshold_3")
    val scoreHighThreshold3: Int,

    @SerializedName("score_low_threshold_1")
    val scoreLowThreshold1: Int,

    @SerializedName("score_low_threshold_2")
    val scoreLowThreshold2: Int
)
