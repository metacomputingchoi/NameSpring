// utils/JsonLoader.kt
package com.ssc.namespring.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.data.json.*
import com.ssc.namespring.utils.logger.AndroidLogger
import java.io.InputStreamReader

object JsonLoader {
    private val logger = AndroidLogger("JsonLoader")
    private val gson = Gson()

    private var isInitialized = false
    lateinit var scoreEvaluations: ScoreEvaluations
    lateinit var strokeMeanings: StrokeMeanings
    lateinit var elementCharacteristics: ElementCharacteristics
    lateinit var sajuAnalyzerStrings: SajuAnalyzerStrings
    lateinit var elementAnalyzerStrings: ElementAnalyzerStrings
    lateinit var yinyangAnalyzerStrings: YinyangAnalyzerStrings
    lateinit var hanjaMeanings: HanjaMeanings
    lateinit var characterMeaningStrings: CharacterMeaningStrings
    lateinit var reportTemplates: ReportTemplates
    lateinit var basicReportSections: BasicReportSections
    lateinit var formatSettings: FormatSettings

    var personalityEvaluatorStrings: PersonalityEvaluatorStrings? = null
    var careerEvaluatorStrings: CareerEvaluatorStrings? = null
    var fortuneEvaluatorStrings: FortuneEvaluatorStrings? = null
    var lifePeriods: LifePeriods? = null
    var personalityTraits: PersonalityTraits? = null
    var businessLuckStrokes: BusinessLuckStrokes? = null
    var careerFields: CareerFields? = null

    fun initialize(context: Context) {
        if (isInitialized) {
            logger.d("JsonLoader already initialized")
            return
        }

        try {
            logger.d("Loading JSON files...")

            scoreEvaluations = loadJsonRequired(context, "evaluations/score_evaluations.json")
            strokeMeanings = loadJsonRequired(context, "evaluations/stroke_meanings.json")

            sajuAnalyzerStrings = loadJsonRequired(context, "analysis/saju_analyzer_strings.json")
            elementAnalyzerStrings = loadJsonRequired(context, "analysis/element_analyzer_strings.json")
            yinyangAnalyzerStrings = loadJsonRequired(context, "analysis/yinyang_analyzer_strings.json")
            characterMeaningStrings = loadJsonRequired(context, "analysis/character_meaning_strings.json")

            elementCharacteristics = loadJsonRequired(context, "meanings/element_characteristics.json")
            hanjaMeanings = loadJsonRequired(context, "meanings/hanja_meanings.json")

            reportTemplates = loadJsonRequired(context, "report/report_templates.json")
            basicReportSections = loadJsonRequired(context, "report/basic_report_sections.json")
            formatSettings = loadJsonRequired(context, "report/format_settings.json")

            personalityEvaluatorStrings = loadJsonOptional(context, "evaluations/personality_evaluator_strings.json")
            careerEvaluatorStrings = loadJsonOptional(context, "evaluations/career_evaluator_strings.json")
            fortuneEvaluatorStrings = loadJsonOptional(context, "evaluations/fortune_evaluator_strings.json")
            businessLuckStrokes = loadJsonOptional(context, "evaluations/business_luck_strokes.json")

            lifePeriods = loadJsonOptional(context, "report/life_periods.json")
            personalityTraits = loadJsonOptional(context, "report/personality_traits.json")
            careerFields = loadJsonOptional(context, "report/career_fields.json")

            isInitialized = true
            logger.d("All JSON files loaded successfully")

        } catch (e: Exception) {
            logger.e("Failed to load JSON files", e)
            throw RuntimeException("JSON 파일 로딩 실패: ${e.message}", e)
        }
    }

    private inline fun <reified T> loadJsonRequired(context: Context, fileName: String): T {
        return try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    gson.fromJson(reader, T::class.java)
                }
            }
        } catch (e: Exception) {
            logger.e("Failed to load required file: $fileName", e)
            throw e
        }
    }

    private inline fun <reified T> loadJsonOptional(context: Context, fileName: String): T? {
        return try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    gson.fromJson(reader, T::class.java)
                }
            }
        } catch (e: Exception) {
            logger.d("Optional file not found: $fileName")
            null
        }
    }

    fun getStrokeMeaning(stroke: Int): StrokeMeaningDetail {
        val normalizedStroke = if (stroke > 81) stroke % 81 else stroke
        val strokeStr = normalizedStroke.toString()

        return strokeMeanings.strokeMeanings[strokeStr]
            ?: strokeMeanings.strokeMeanings["1"]!!
    }

    fun getElementCharacteristic(element: String): String {
        return elementCharacteristics.elementCharacteristics[element]
            ?: "알 수 없는 오행"
    }

    fun getGrade(score: Int): String {
        return when {
            score >= scoreEvaluations.scoreThresholds.gradeA -> "A"
            score >= scoreEvaluations.scoreThresholds.gradeB -> "B"
            score >= scoreEvaluations.scoreThresholds.gradeC -> "C"
            else -> "D"
        }
    }

    fun getHanjaMeaning(hanja: String): HanjaInfo? {
        return HanjaInfo(
            origin = hanjaMeanings.hanjaOrigins[hanja],
            components = hanjaMeanings.hanjaComponents[hanja],
            relatedCharacters = hanjaMeanings.hanjaRelatedCharacters[hanja]
        )
    }

    fun isBusinessLuckStroke(stroke: Int): Boolean {
        val normalizedStroke = if (stroke > 81) stroke % 81 else stroke
        return businessLuckStrokes?.businessLuckStrokes?.contains(normalizedStroke) ?: false
    }

    fun isLeadershipStroke(stroke: Int): Boolean {
        val normalizedStroke = if (stroke > 81) stroke % 81 else stroke
        return businessLuckStrokes?.leadershipStrokes?.contains(normalizedStroke) ?: false
    }

    fun hasPositiveMeaning(meaning: String): Boolean {
        return hanjaMeanings.positiveMeanings.any { positive ->
            meaning.contains(positive)
        }
    }

    fun isMeaningHarmony(meaning1: String, meaning2: String): Boolean {
        val pattern = "${meaning1}_${meaning2}"
        return hanjaMeanings.meaningHarmonyPatterns[pattern] ?: false
    }

    fun getReportSectionTitle(section: String): String {
        return reportTemplates.sectionTitles[section] ?: section
    }

    fun getReportSubsectionLabel(subsection: String): String {
        return reportTemplates.subsectionLabels[subsection] ?: subsection
    }
}
