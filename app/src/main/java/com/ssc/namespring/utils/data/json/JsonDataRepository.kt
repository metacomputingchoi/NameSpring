// utils/data/json/JsonDataRepository.kt
package com.ssc.namespring.utils.data.json

import com.ssc.namespring.model.data.source.BasicReportSections
import com.ssc.namespring.model.data.source.BusinessLuckStrokes
import com.ssc.namespring.model.data.source.CareerEvaluatorStrings
import com.ssc.namespring.model.data.source.CareerFields
import com.ssc.namespring.model.data.source.CharacterMeaningStrings
import com.ssc.namespring.model.data.source.ElementAnalyzerStrings
import com.ssc.namespring.model.data.source.ElementCharacteristics
import com.ssc.namespring.model.data.source.FormatSettings
import com.ssc.namespring.model.data.source.FortuneEvaluatorStrings
import com.ssc.namespring.model.data.source.HanjaMeanings
import com.ssc.namespring.model.data.source.LifePeriods
import com.ssc.namespring.model.data.source.PersonalityEvaluatorStrings
import com.ssc.namespring.model.data.source.PersonalityTraits
import com.ssc.namespring.model.data.source.ReportTemplates
import com.ssc.namespring.model.data.source.SajuAnalyzerStrings
import com.ssc.namespring.model.data.source.ScoreEvaluations
import com.ssc.namespring.model.data.source.StrokeMeanings
import com.ssc.namespring.model.data.source.YinyangAnalyzerStrings

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