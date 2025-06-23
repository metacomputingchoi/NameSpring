// model/application/service/report/data/ReportData.kt
package com.ssc.namespring.model.application.service.report.data

import com.google.gson.annotations.SerializedName

// Stroke Meanings
data class StrokeMeaningData(
    val number: Int,
    val title: String,
    val summary: String,
    @SerializedName("detailed_explanation") val detailedExplanation: String,
    @SerializedName("positive_aspects") val positiveAspects: String,
    @SerializedName("caution_points") val cautionPoints: String,
    @SerializedName("personality_traits") val personalityTraits: List<String>,
    @SerializedName("suitable_career") val suitableCareer: List<String>,
    @SerializedName("life_period_influence") val lifePeriodInfluence: String,
    @SerializedName("special_characteristics") val specialCharacteristics: String? = null,
    @SerializedName("challenge_period") val challengePeriod: String? = null,
    @SerializedName("opportunity_area") val opportunityArea: String? = null
)

data class StrokeMeaningsData(
    @SerializedName("stroke_meanings") val strokeMeanings: Map<String, StrokeMeaningData>
)

// Element Data
data class ElementCharacteristicsData(
    @SerializedName("element_characteristics") val elementCharacteristics: Map<String, String>,
    @SerializedName("element_colors") val elementColors: Map<String, List<String>>,
    @SerializedName("element_generative_relations") val elementGenerativeRelations: Map<String, String>,
    @SerializedName("element_controlling_relations") val elementControllingRelations: Map<String, String>,
    @SerializedName("element_career_fields") val elementCareerFields: Map<String, String>,
    @SerializedName("element_development_areas") val elementDevelopmentAreas: Map<String, String>,
    @SerializedName("element_lacking_recommendations") val elementLackingRecommendations: Map<String, String>
)

// Score Evaluations
data class ScoreEvaluationsData(
    @SerializedName("score_thresholds") val scoreThresholds: Map<String, Int>,
    @SerializedName("overall_evaluations") val overallEvaluations: Map<String, String>,
    @SerializedName("grade_assessments") val gradeAssessments: Map<String, String>,
    val recommendations: Map<String, String>,
    @SerializedName("strength_messages") val strengthMessages: Map<String, String>,
    @SerializedName("weakness_messages") val weaknessMessages: Map<String, String>,
    @SerializedName("detailed_recommendations") val detailedRecommendations: Map<String, String>
)

// Business Luck
data class BusinessLuckData(
    @SerializedName("business_luck_strokes") val businessLuckStrokes: List<Int>,
    @SerializedName("leadership_strokes") val leadershipStrokes: List<Int>,
    @SerializedName("unsuitable_for_stability") val unsuitableForStability: List<Int>,
    @SerializedName("unsuitable_for_adventure") val unsuitableForAdventure: List<Int>,
    @SerializedName("business_evaluations") val businessEvaluations: Map<String, String>,
    @SerializedName("leadership_evaluations") val leadershipEvaluations: Map<String, String>
)

// Pillar Interpretations
data class PillarInterpretationsData(
    @SerializedName("pillar_interpretations") val pillarInterpretations: Map<String, String>,
    @SerializedName("seasonal_influences") val seasonalInfluences: Map<String, SeasonalInfluence>,
    @SerializedName("element_balance_descriptions") val elementBalanceDescriptions: Map<String, String>,
    @SerializedName("day_master_evaluations") val dayMasterEvaluations: Map<String, String>
)

data class SeasonalInfluence(
    val branches: List<String>,
    val influence: String
)

// Yin Yang
data class YinYangPatternsData(
    @SerializedName("yin_yang_transitions") val yinYangTransitions: Map<String, String>,
    @SerializedName("pattern_analyses") val patternAnalyses: Map<String, String>,
    @SerializedName("energy_types") val energyTypes: Map<String, String>,
    @SerializedName("emotional_tendencies") val emotionalTendencies: Map<String, String>
)

// Report Templates
data class ReportTemplatesData(
    @SerializedName("report_headers") val reportHeaders: Map<String, Any>,
    @SerializedName("section_titles") val sectionTitles: Map<String, String>,
    @SerializedName("subsection_labels") val subsectionLabels: Map<String, String>,
    @SerializedName("footer_messages") val footerMessages: Map<String, String>,
    @SerializedName("score_display") val scoreDisplay: Map<String, String>
)

// Hanja Meanings
data class HanjaMeaningsData(
    @SerializedName("hanja_origins") val hanjaOrigins: Map<String, String>,
    @SerializedName("hanja_components") val hanjaComponents: Map<String, List<String>>,
    @SerializedName("hanja_related_characters") val hanjaRelatedCharacters: Map<String, List<String>>,
    @SerializedName("combined_meanings") val combinedMeanings: Map<String, String>,
    @SerializedName("symbolic_elements") val symbolicElements: Map<String, String>,
    @SerializedName("cultural_significance") val culturalSignificance: String,
    @SerializedName("positive_meanings") val positiveMeanings: List<String>,
    @SerializedName("meaning_harmony_patterns") val meaningHarmonyPatterns: Map<String, Boolean>
)

// Constants
data class ConstantsData(
    @SerializedName("report_constants") val reportConstants: Map<String, Any>,
    @SerializedName("four_types_names") val fourTypesNames: List<String>,
    @SerializedName("name_positions") val namePositions: Map<String, String>
)

// Life Flow
data class LifeFlowData(
    @SerializedName("life_flow_by_total") val lifeFlowByTotal: Map<String, String>,
    @SerializedName("cycle_types") val cycleTypes: Map<String, String>,
    @SerializedName("cycle_interpretations") val cycleInterpretations: Map<String, String>
)

// Element Relations
data class ElementRelationsData(
    @SerializedName("relation_types") val relationTypes: Map<String, RelationType>,
    @SerializedName("position_labels") val positionLabels: Map<String, String>,
    @SerializedName("element_strength_descriptions") val elementStrengthDescriptions: Map<String, String>
)

data class RelationType(
    val name: String,
    val description: String
)

// Personality Traits
data class PersonalityTraitsData(
    @SerializedName("social_styles") val socialStyles: Map<String, String>,
    @SerializedName("work_styles") val workStyles: Map<String, String>,
    @SerializedName("weakness_transformations") val weaknessTransformations: Map<String, String>
)

// Career Fields
data class CareerFieldsData(
    @SerializedName("avoidance_strokes") val avoidanceStrokes: AvoidanceStrokes
)

data class AvoidanceStrokes(
    @SerializedName("high_risk") val highRisk: List<Int>,
    @SerializedName("avoid_message") val avoidMessage: String,
    @SerializedName("stability_unsuitable") val stabilityUnsuitable: List<Int>,
    @SerializedName("stability_message") val stabilityMessage: String
)

// Life Periods
data class LifePeriodsData(
    @SerializedName("life_period_names") val lifePeriodNames: Map<String, String>,
    @SerializedName("life_period_mapping") val lifePeriodMapping: Map<String, String>
)

// Format Settings
data class FormatSettingsData(
    @SerializedName("progress_bar") val progressBar: Map<String, Any>,
    @SerializedName("dot_display") val dotDisplay: Map<String, Any>,
    val separators: Map<String, String>,
    val prefixes: Map<String, String>,
    @SerializedName("section_numbers") val sectionNumbers: Map<String, String>
)

// Basic Report Sections
data class BasicReportSectionsData(
    val sections: List<String>,
    @SerializedName("score_categories") val scoreCategories: List<ScoreCategory>
)

data class ScoreCategory(
    val name: String,
    @SerializedName("max_score") val maxScore: Int,
    val field: String
)

// NEW DATA CLASSES FOR ANALYZER STRINGS

// Element Analyzer Strings
data class ElementAnalyzerStringsData(
    @SerializedName("base_analysis") val baseAnalysis: String,
    @SerializedName("harmony_relations") val harmonyRelations: Map<String, String>,
    @SerializedName("position_labels") val positionLabels: Map<String, String>,
    @SerializedName("relation_errors") val relationErrors: Map<String, String>,
    @SerializedName("default_relations") val defaultRelations: Map<String, String>,
    @SerializedName("cycle_analysis") val cycleAnalysis: Map<String, String>,
    @SerializedName("default_position") val defaultPosition: String,
    @SerializedName("strength_error") val strengthError: String,
    @SerializedName("name_format") val nameFormat: String,
    @SerializedName("magic_numbers") val magicNumbers: Map<String, Int>
)

// Saju Analyzer Strings
data class SajuAnalyzerStringsData(
    @SerializedName("base_analysis_template") val baseAnalysisTemplate: String,
    @SerializedName("supplement_analysis") val supplementAnalysis: Map<String, String>,
    @SerializedName("pillar_names") val pillarNames: Map<String, String>,
    @SerializedName("default_values") val defaultValues: Map<String, String>,
    @SerializedName("day_master_template") val dayMasterTemplate: String,
    @SerializedName("pillar_format") val pillarFormat: String,
    @SerializedName("dominant_element_format") val dominantElementFormat: String,
    @SerializedName("magic_numbers") val magicNumbers: Map<String, Double>
)

// Stroke Analyzer Strings
data class StrokeAnalyzerStringsData(
    @SerializedName("basic_analysis") val basicAnalysis: Map<String, String>,
    @SerializedName("stroke_names") val strokeNames: Map<String, String>,
    @SerializedName("stroke_format") val strokeFormat: String,
    @SerializedName("luck_types") val luckTypes: Map<String, String>,
    @SerializedName("luck_analysis_format") val luckAnalysisFormat: String,
    @SerializedName("default_meaning") val defaultMeaning: Map<String, Any>,
    @SerializedName("life_path_analysis") val lifePathAnalysis: Map<String, String>,
    @SerializedName("magic_numbers") val magicNumbers: Map<String, Int>
)

// YinYang Analyzer Strings
data class YinYangAnalyzerStringsData(
    @SerializedName("error_message") val errorMessage: String,
    @SerializedName("balance_template") val balanceTemplate: String,
    @SerializedName("balance_patterns") val balancePatterns: Map<String, String>,
    @SerializedName("default_pattern") val defaultPattern: String,
    @SerializedName("unique_pattern_message") val uniquePatternMessage: String,
    @SerializedName("yin_char") val yinChar: String,
    @SerializedName("yang_char") val yangChar: String,
    @SerializedName("balance_scores") val balanceScores: Map<String, Int>,
    @SerializedName("balance_thresholds") val balanceThresholds: Map<String, Int>,
    @SerializedName("magic_numbers") val magicNumbers: Map<String, Int>
)

// Career Evaluator Strings
data class CareerEvaluatorStringsData(
    @SerializedName("success_factors") val successFactors: Map<String, String>,
    @SerializedName("work_style_keywords") val workStyleKeywords: Map<String, String>,
    @SerializedName("personality_keywords") val personalityKeywords: Map<String, String>,
    val thresholds: Map<String, Int>,
    @SerializedName("magic_numbers") val magicNumbers: Map<String, Int>
)

// Fortune Evaluator Strings
data class FortuneEvaluatorStringsData(
    @SerializedName("example_names") val exampleNames: List<String>,
    @SerializedName("life_periods") val lifePeriods: Map<String, String>,
    @SerializedName("period_names") val periodNames: Map<String, String>,
    @SerializedName("lucky_element_format") val luckyElementFormat: String,
    @SerializedName("energy_format") val energyFormat: String,
    @SerializedName("challenge_format") val challengeFormat: String,
    @SerializedName("business_opportunity") val businessOpportunity: String,
    @SerializedName("period_influence_format") val periodInfluenceFormat: String,
    val thresholds: Map<String, Int>,
    @SerializedName("element_cycle") val elementCycle: Map<String, String>
)

// Personality Evaluator Strings
data class PersonalityEvaluatorStringsData(
    @SerializedName("social_index") val socialIndex: Int,
    val thresholds: Map<String, Int>
)

// Score Evaluator Strings
data class ScoreEvaluatorStringsData(
    val grades: Map<String, Int>,
    @SerializedName("default_grade") val defaultGrade: String,
    @SerializedName("error_message") val errorMessage: String,
    @SerializedName("evaluation_format") val evaluationFormat: String,
    @SerializedName("default_strengths") val defaultStrengths: String,
    @SerializedName("default_weaknesses") val defaultWeaknesses: String,
    @SerializedName("list_prefix") val listPrefix: String
)

// Character Meaning Strings
data class CharacterMeaningStringsData(
    @SerializedName("default_meaning") val defaultMeaning: String,
    @SerializedName("origin_not_found") val originNotFound: String,
    @SerializedName("combined_meaning_format") val combinedMeaningFormat: String,
    @SerializedName("default_symbolic") val defaultSymbolic: String,
    @SerializedName("basic_score") val basicScore: Int,
    @SerializedName("positive_meaning_bonus") val positiveMeaningBonus: Int,
    @SerializedName("harmony_bonus") val harmonyBonus: Int,
    @SerializedName("score_min") val scoreMin: Int,
    @SerializedName("score_max") val scoreMax: Int,
    @SerializedName("harmony_pattern_size") val harmonyPatternSize: Int,
    @SerializedName("pattern_delimiter") val patternDelimiter: String
)

// Stroke Meaning Strings
data class StrokeMeaningStringsData(
    @SerializedName("stroke_types") val strokeTypes: Map<String, Map<String, String>>,
    @SerializedName("caution_format") val cautionFormat: String,
    @SerializedName("line_separator") val lineSeparator: String,
    @SerializedName("modulo_base") val moduloBase: Int
)

// Report Builder Strings
data class ReportBuilderStringsData(
    @SerializedName("summary_format") val summaryFormat: String,
    @SerializedName("pronunciation_error") val pronunciationError: String,
    @SerializedName("pronunciation_natural") val pronunciationNatural: String,
    @SerializedName("pronunciation_unnatural") val pronunciationUnnatural: String,
    @SerializedName("comprehensive_prefix") val comprehensivePrefix: String,
    @SerializedName("score_categories") val scoreCategories: Map<String, String>,
    @SerializedName("max_scores") val maxScores: Map<String, Int>,
    @SerializedName("total_max_score") val totalMaxScore: Int,
    @SerializedName("hangul_naturalness_step") val hangulNaturalnessStep: String
)

// Basic Formatter Strings
data class BasicFormatterStringsData(
    @SerializedName("separator_length") val separatorLength: Int,
    val title: String,
    @SerializedName("header_format") val headerFormat: String,
    @SerializedName("section_prefixes") val sectionPrefixes: Map<String, String>,
    @SerializedName("score_format") val scoreFormat: String,
    @SerializedName("total_separator") val totalSeparator: String,
    @SerializedName("total_format") val totalFormat: String,
    @SerializedName("section_suffix") val sectionSuffix: String,
    @SerializedName("recommendation_prefix") val recommendationPrefix: String,
    @SerializedName("hanja_info_format") val hanjaInfoFormat: String,
    @SerializedName("hanja_details") val hanjaDetails: Map<String, String>,
    @SerializedName("separator_char") val separatorChar: String,
    @SerializedName("score_fields") val scoreFields: Map<String, String>
)

// Detailed Formatter Strings
data class DetailedFormatterStringsData(
    @SerializedName("separator_length") val separatorLength: Int,
    val title: String,
    @SerializedName("header_format") val headerFormat: String,
    val separators: Map<String, String>,
    @SerializedName("section_separator_length") val sectionSeparatorLength: Int,
    val prefixes: Map<String, String>,
    @SerializedName("info_formats") val infoFormats: Map<String, String>,
    @SerializedName("progress_bar") val progressBar: Map<String, Any>,
    @SerializedName("dot_display") val dotDisplay: Map<String, Any>,
    @SerializedName("saju_formats") val sajuFormats: Map<String, String>,
    @SerializedName("stroke_formats") val strokeFormats: Map<String, String>,
    @SerializedName("element_formats") val elementFormats: Map<String, String>,
    @SerializedName("yinyang_formats") val yinyangFormats: Map<String, String>,
    @SerializedName("character_formats") val characterFormats: Map<String, String>,
    @SerializedName("list_formats") val listFormats: Map<String, String>,
    @SerializedName("footer_disclaimer_prefix") val footerDisclaimerPrefix: String,
    @SerializedName("magic_numbers") val magicNumbers: Map<String, Int>
)