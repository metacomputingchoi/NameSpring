// utils/json/JsonDataRepository.kt
package com.ssc.namespring.utils.json

import com.ssc.namespring.model.data.json.*

internal class JsonDataRepository {
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
}