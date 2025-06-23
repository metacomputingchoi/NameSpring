// model/application/service/report/data/ReportDataHolder.kt
package com.ssc.namespring.model.application.service.report.data

import android.content.Context
import com.ssc.namespring.model.application.service.report.utils.JsonAssetReader

object ReportDataHolder {
    private lateinit var jsonReader: JsonAssetReader

    lateinit var strokeMeaningsData: StrokeMeaningsData
    lateinit var elementCharacteristicsData: ElementCharacteristicsData
    lateinit var scoreEvaluationsData: ScoreEvaluationsData
    lateinit var businessLuckData: BusinessLuckData
    lateinit var pillarInterpretationsData: PillarInterpretationsData
    lateinit var yinYangPatternsData: YinYangPatternsData
    lateinit var reportTemplatesData: ReportTemplatesData
    lateinit var hanjaMeaningsData: HanjaMeaningsData
    lateinit var constantsData: ConstantsData
    lateinit var lifeFlowData: LifeFlowData
    lateinit var elementRelationsData: ElementRelationsData
    lateinit var personalityTraitsData: PersonalityTraitsData
    lateinit var careerFieldsData: CareerFieldsData
    lateinit var lifePeriodsData: LifePeriodsData
    lateinit var formatSettingsData: FormatSettingsData
    lateinit var basicReportSectionsData: BasicReportSectionsData

    // NEW: Analyzer and formatter strings
    lateinit var elementAnalyzerStrings: ElementAnalyzerStringsData
    lateinit var sajuAnalyzerStrings: SajuAnalyzerStringsData
    lateinit var strokeAnalyzerStrings: StrokeAnalyzerStringsData
    lateinit var yinYangAnalyzerStrings: YinYangAnalyzerStringsData
    lateinit var careerEvaluatorStrings: CareerEvaluatorStringsData
    lateinit var fortuneEvaluatorStrings: FortuneEvaluatorStringsData
    lateinit var personalityEvaluatorStrings: PersonalityEvaluatorStringsData
    lateinit var scoreEvaluatorStrings: ScoreEvaluatorStringsData
    lateinit var characterMeaningStrings: CharacterMeaningStringsData
    lateinit var strokeMeaningStrings: StrokeMeaningStringsData
    lateinit var reportBuilderStrings: ReportBuilderStringsData
    lateinit var basicFormatterStrings: BasicFormatterStringsData
    lateinit var detailedFormatterStrings: DetailedFormatterStringsData

    fun initialize(context: Context) {
        jsonReader = JsonAssetReader(context)

        strokeMeaningsData = jsonReader.readAsset("meanings/stroke_meanings.json", StrokeMeaningsData::class.java)
        elementCharacteristicsData = jsonReader.readAsset("meanings/element_characteristics.json", ElementCharacteristicsData::class.java)
        scoreEvaluationsData = jsonReader.readAsset("evaluations/score_evaluations.json", ScoreEvaluationsData::class.java)
        businessLuckData = jsonReader.readAsset("evaluations/business_luck_strokes.json", BusinessLuckData::class.java)
        pillarInterpretationsData = jsonReader.readAsset("report/pillar_interpretations.json", PillarInterpretationsData::class.java)
        yinYangPatternsData = jsonReader.readAsset("report/yin_yang_patterns.json", YinYangPatternsData::class.java)
        reportTemplatesData = jsonReader.readAsset("report/report_templates.json", ReportTemplatesData::class.java)
        hanjaMeaningsData = jsonReader.readAsset("report/hanja_meanings.json", HanjaMeaningsData::class.java)
        constantsData = jsonReader.readAsset("report/constants.json", ConstantsData::class.java)
        lifeFlowData = jsonReader.readAsset("report/life_flow_interpretations.json", LifeFlowData::class.java)
        elementRelationsData = jsonReader.readAsset("report/element_relations.json", ElementRelationsData::class.java)
        personalityTraitsData = jsonReader.readAsset("report/personality_traits.json", PersonalityTraitsData::class.java)
        careerFieldsData = jsonReader.readAsset("report/career_fields.json", CareerFieldsData::class.java)
        lifePeriodsData = jsonReader.readAsset("report/life_periods.json", LifePeriodsData::class.java)
        formatSettingsData = jsonReader.readAsset("report/format_settings.json", FormatSettingsData::class.java)
        basicReportSectionsData = jsonReader.readAsset("report/basic_report_sections.json", BasicReportSectionsData::class.java)

        // NEW: Load analyzer and formatter strings
        elementAnalyzerStrings = jsonReader.readAsset("analyzer/element_analyzer_strings.json", ElementAnalyzerStringsData::class.java)
        sajuAnalyzerStrings = jsonReader.readAsset("analyzer/saju_analyzer_strings.json", SajuAnalyzerStringsData::class.java)
        strokeAnalyzerStrings = jsonReader.readAsset("analyzer/stroke_analyzer_strings.json", StrokeAnalyzerStringsData::class.java)
        yinYangAnalyzerStrings = jsonReader.readAsset("analyzer/yinyang_analyzer_strings.json", YinYangAnalyzerStringsData::class.java)
        careerEvaluatorStrings = jsonReader.readAsset("evaluator/career_evaluator_strings.json", CareerEvaluatorStringsData::class.java)
        fortuneEvaluatorStrings = jsonReader.readAsset("evaluator/fortune_evaluator_strings.json", FortuneEvaluatorStringsData::class.java)
        personalityEvaluatorStrings = jsonReader.readAsset("evaluator/personality_evaluator_strings.json", PersonalityEvaluatorStringsData::class.java)
        scoreEvaluatorStrings = jsonReader.readAsset("evaluator/score_evaluator_strings.json", ScoreEvaluatorStringsData::class.java)
        characterMeaningStrings = jsonReader.readAsset("analyzer/character_meaning_strings.json", CharacterMeaningStringsData::class.java)
        strokeMeaningStrings = jsonReader.readAsset("analyzer/stroke_meaning_strings.json", StrokeMeaningStringsData::class.java)
        reportBuilderStrings = jsonReader.readAsset("builder/report_builder_strings.json", ReportBuilderStringsData::class.java)
        basicFormatterStrings = jsonReader.readAsset("formatter/basic_formatter_strings.json", BasicFormatterStringsData::class.java)
        detailedFormatterStrings = jsonReader.readAsset("formatter/detailed_formatter_strings.json", DetailedFormatterStringsData::class.java)
    }
}