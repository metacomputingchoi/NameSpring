// model/application/service/report/analyzer/meaning/StrokeMeaningAnalyzer.kt
package com.ssc.namespring.model.application.service.report.analyzer.meaning

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class StrokeMeaningAnalyzer {
    private val strokeMeanings = ReportDataHolder.strokeMeaningsData.strokeMeanings
    private val strings = ReportDataHolder.strokeMeaningStrings

    fun getComprehensiveMeaning(result: Name): String {
        val analysis = result.combinationAnalysis
        val meanings = mutableListOf<String>()

        val personalityStroke = analysis.fourTypes[0]
        val personalityMeaning = getMeaning(personalityStroke)
        meanings.add(strings.strokeTypes["personality"]!!["prefix"]!! + personalityStroke +
                strings.strokeTypes["personality"]!!["suffix"]!! + personalityMeaning.summary)

        val foundationStroke = analysis.fourTypes[1]
        val foundationMeaning = getMeaning(foundationStroke)
        meanings.add(strings.strokeTypes["foundation"]!!["prefix"]!! + foundationStroke +
                strings.strokeTypes["foundation"]!!["suffix"]!! + foundationMeaning.summary)

        val socialStroke = analysis.fourTypes[2]
        val socialMeaning = getMeaning(socialStroke)
        meanings.add(strings.strokeTypes["social"]!!["prefix"]!! + socialStroke +
                strings.strokeTypes["social"]!!["suffix"]!! + socialMeaning.summary)

        val totalStroke = analysis.fourTypes[3]
        val totalMeaning = getMeaning(totalStroke)
        meanings.add(strings.strokeTypes["total"]!!["prefix"]!! + totalStroke +
                strings.strokeTypes["total"]!!["suffix"]!! + totalMeaning.summary)

        return meanings.joinToString(strings.lineSeparator)
    }

    fun getSpecialCharacteristics(strokes: List<Int>): List<String> {
        val characteristics = mutableListOf<String>()

        strokes.forEach { stroke ->
            val meaning = getMeaning(stroke)
            meaning.specialCharacteristics?.let { characteristics.add(it) }
        }

        return characteristics.distinct()
    }

    fun getCautionPoints(strokes: List<Int>): List<String> {
        val cautionPoints = mutableListOf<String>()

        strokes.forEach { stroke ->
            val meaning = getMeaning(stroke)
            if (meaning.cautionPoints.isNotEmpty()) {
                cautionPoints.add(strings.cautionFormat
                    .replace("{stroke}", stroke.toString())
                    .replace("{caution}", meaning.cautionPoints))
            }
        }

        return cautionPoints
    }

    private fun getMeaning(stroke: Int): com.ssc.namespring.model.application.service.report.data.StrokeMeaningData {
        val moduloBase = strings.moduloBase
        val defaultMeaning = ReportDataHolder.strokeAnalyzerStrings.defaultMeaning

        return strokeMeanings[stroke.toString()]
            ?: strokeMeanings[(stroke % moduloBase).toString()]
            ?: com.ssc.namespring.model.application.service.report.data.StrokeMeaningData(
                number = stroke,
                title = defaultMeaning["title"] as String,
                summary = defaultMeaning["summary"] as String,
                detailedExplanation = defaultMeaning["detailed_explanation"] as String,
                positiveAspects = defaultMeaning["positive_aspects"] as String,
                cautionPoints = defaultMeaning["caution_points"] as String,
                personalityTraits = @Suppress("UNCHECKED_CAST") (defaultMeaning["personality_traits"] as List<String>),
                suitableCareer = @Suppress("UNCHECKED_CAST") (defaultMeaning["suitable_career"] as List<String>),
                lifePeriodInfluence = defaultMeaning["life_period_influence"] as String
            )
    }
}