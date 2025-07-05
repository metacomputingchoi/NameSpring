// utils/data/json/JsonDataInitializer.kt
package com.ssc.namespring.utils.data.json

import com.ssc.namespring.utils.logger.AndroidLogger

internal class JsonDataInitializer(
    private val loader: JsonFileLoader,
    private val repository: JsonDataRepository
) {
    private val logger = AndroidLogger("JsonDataInitializer")

    fun initialize() {
        try {
            logger.d("Loading JSON files...")
            loadRequiredFiles()
            loadOptionalFiles()
            logger.d("All JSON files loaded successfully")
        } catch (e: Exception) {
            logger.e("Failed to load JSON files", e)
            throw RuntimeException("JSON 파일 로딩 실패: ${e.message}", e)
        }
    }

    private fun loadRequiredFiles() {
        repository.scoreEvaluations = loader.loadRequired("evaluations/score_evaluations.json")
        repository.strokeMeanings = loader.loadRequired("evaluations/stroke_meanings.json")
        repository.sajuAnalyzerStrings = loader.loadRequired("analysis/saju_analyzer_strings.json")
        repository.elementAnalyzerStrings = loader.loadRequired("analysis/element_analyzer_strings.json")
        repository.yinyangAnalyzerStrings = loader.loadRequired("analysis/yinyang_analyzer_strings.json")
        repository.characterMeaningStrings = loader.loadRequired("analysis/character_meaning_strings.json")
        repository.elementCharacteristics = loader.loadRequired("meanings/element_characteristics.json")
        repository.hanjaMeanings = loader.loadRequired("meanings/hanja_meanings.json")
        repository.reportTemplates = loader.loadRequired("report/report_templates.json")
        repository.basicReportSections = loader.loadRequired("report/basic_report_sections.json")
        repository.formatSettings = loader.loadRequired("report/format_settings.json")
    }

    private fun loadOptionalFiles() {
        repository.personalityEvaluatorStrings = loader.loadOptional("evaluations/personality_evaluator_strings.json")
        repository.careerEvaluatorStrings = loader.loadOptional("evaluations/career_evaluator_strings.json")
        repository.fortuneEvaluatorStrings = loader.loadOptional("evaluations/fortune_evaluator_strings.json")
        repository.businessLuckStrokes = loader.loadOptional("evaluations/business_luck_strokes.json")
        repository.lifePeriods = loader.loadOptional("report/life_periods.json")
        repository.personalityTraits = loader.loadOptional("report/personality_traits.json")
        repository.careerFields = loader.loadOptional("report/career_fields.json")
    }
}