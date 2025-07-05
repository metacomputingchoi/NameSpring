// model/data/source/AnalyzerStrings.kt
package com.ssc.namespring.model.data.source

import com.google.gson.annotations.SerializedName

data class SajuAnalyzerStrings(
    @SerializedName("base_analysis_template")
    val baseAnalysisTemplate: String,

    @SerializedName("supplement_analysis")
    val supplementAnalysis: Map<String, String>,

    @SerializedName("pillar_names")
    val pillarNames: Map<String, String>,

    @SerializedName("default_values")
    val defaultValues: Map<String, String>,

    @SerializedName("day_master_template")
    val dayMasterTemplate: String,

    @SerializedName("pillar_format")
    val pillarFormat: String,

    @SerializedName("dominant_element_format")
    val dominantElementFormat: String,

    @SerializedName("magic_numbers")
    val magicNumbers: Map<String, Double>
)

data class ElementAnalyzerStrings(
    @SerializedName("base_analysis")
    val baseAnalysis: String,

    @SerializedName("harmony_relations")
    val harmonyRelations: Map<String, String>,

    @SerializedName("position_labels")
    val positionLabels: Map<String, String>,

    @SerializedName("relation_errors")
    val relationErrors: Map<String, String>,

    @SerializedName("default_relations")
    val defaultRelations: Map<String, String>,

    @SerializedName("cycle_analysis")
    val cycleAnalysis: Map<String, String>,

    @SerializedName("default_position")
    val defaultPosition: String,

    @SerializedName("strength_error")
    val strengthError: String,

    @SerializedName("name_format")
    val nameFormat: String,

    @SerializedName("magic_numbers")
    val magicNumbers: Map<String, Int>
)

data class YinyangAnalyzerStrings(
    @SerializedName("error_message")
    val errorMessage: String,

    @SerializedName("balance_template")
    val balanceTemplate: String,

    @SerializedName("balance_patterns")
    val balancePatterns: Map<String, String>,

    @SerializedName("default_pattern")
    val defaultPattern: String,

    @SerializedName("unique_pattern_message")
    val uniquePatternMessage: String,

    @SerializedName("yin_char")
    val yinChar: String,

    @SerializedName("yang_char")
    val yangChar: String,

    @SerializedName("balance_scores")
    val balanceScores: Map<String, Int>,

    @SerializedName("balance_thresholds")
    val balanceThresholds: Map<String, Int>,

    @SerializedName("magic_numbers")
    val magicNumbers: Map<String, Int>
)
