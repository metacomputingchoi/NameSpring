// model/application/service/report/analyzer/core/StrokeAnalyzer.kt
package com.ssc.namespring.model.application.service.report.analyzer.core

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.StrokeDetail
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder
import com.ssc.namespring.model.application.service.report.data.StrokeMeaningData

class StrokeAnalyzer {
    private val fourTypesNames = ReportDataHolder.constantsData.fourTypesNames
    private val strokeMeanings = ReportDataHolder.strokeMeaningsData.strokeMeanings
    private val strings = ReportDataHolder.strokeAnalyzerStrings

    fun analyzeBasic(result: Name): String {
        val strokeDetails = buildStrokeDetails(result.combinationAnalysis)
        val luckCount = result.combinationAnalysis.fourTypesLuck.sum()

        return strings.basicAnalysis["stroke_details"]!!.replace("{details}", strokeDetails.joinToString(", ")) +
                "\n" + strings.basicAnalysis["luck_count"]!!.replace("{count}", luckCount.toString())
    }

    fun analyzeDetailed(result: Name): StrokeDetail {
        val analysis = result.combinationAnalysis
        val strokeNumbers = mapOf(
            strings.strokeNames["heaven"]!! to (result.surHanja.length + analysis.surHanjaStroke),
            fourTypesNames[0] to analysis.fourTypes[0],
            fourTypesNames[1] to analysis.fourTypes[1],
            fourTypesNames[2] to analysis.fourTypes[2],
            fourTypesNames[3] to analysis.fourTypes[3]
        )

        return StrokeDetail(
            strokeNumbers = strokeNumbers,
            luckAnalysis = analyzeLuck(analysis),
            numerologyMeaning = analyzeNumerology(strokeNumbers),
            lifePathInfluence = analyzeLifePath(strokeNumbers)
        )
    }

    fun getLuckyNumbers(result: Name): List<Int> {
        val analysis = result.combinationAnalysis
        return analysis.fourTypes.filterIndexed { index, _ ->
            analysis.fourTypesLuck[index] == 1
        }.distinct()
    }

    private fun buildStrokeDetails(analysis: com.ssc.namespring.model.domain.name.entity.NameCombination): List<String> {
        return analysis.fourTypes.mapIndexed { i, value ->
            val isLucky = analysis.fourTypesLuck[i] == 1
            strings.strokeFormat
                .replace("{name}", fourTypesNames[i])
                .replace("{stroke}", value.toString())
                .replace("{luck_type}", if (isLucky) strings.luckTypes["lucky"]!! else strings.luckTypes["unlucky"]!!)
        }
    }

    private fun analyzeLuck(analysis: com.ssc.namespring.model.domain.name.entity.NameCombination): Map<String, String> {
        val names = listOf(strings.strokeNames["personality"]!!, strings.strokeNames["foundation"]!!,
            strings.strokeNames["social"]!!, strings.strokeNames["total"]!!)
        return analysis.fourTypes.mapIndexed { i, stroke ->
            val isLucky = analysis.fourTypesLuck[i] == 1
            val meaning = getMeaning(stroke)

            names[i] to strings.luckAnalysisFormat
                .replace("{stroke}", stroke.toString())
                .replace("{luck_type}", if (isLucky) strings.luckTypes["lucky_formal"]!! else strings.luckTypes["unlucky_formal"]!!)
                .replace("{summary}", meaning.summary)
                .replace("{details}", if (isLucky) meaning.positiveAspects else meaning.cautionPoints)
        }.toMap()
    }

    private fun analyzeNumerology(strokeNumbers: Map<String, Int>): Map<String, String> {
        return strokeNumbers.mapValues { (_, stroke) ->
            getMeaning(stroke).detailedExplanation
        }
    }

    private fun analyzeLifePath(strokeNumbers: Map<String, Int>): String {
        val total = strokeNumbers[strings.strokeNames["total"]!!] ?: 0
        val personality = strokeNumbers[strings.strokeNames["personality"]!!] ?: 0

        val totalMeaning = getMeaning(total)
        val personalityMeaning = getMeaning(personality)

        return strings.lifePathAnalysis["total_flow"]!!
            .replace("{total}", total.toString())
            .replace("{summary}", totalMeaning.summary) +
                strings.lifePathAnalysis["personality_core"]!!
                    .replace("{personality}", personality.toString())
                    .replace("{summary}", personalityMeaning.summary) +
                strings.lifePathAnalysis["overall_influence"]!!
                    .replace("{interpretation}", interpretLifeFlow(total, personality))
    }

    private fun interpretLifeFlow(total: Int, personality: Int): String {
        val lifeFlowData = ReportDataHolder.lifeFlowData.lifeFlowByTotal

        return when {
            total > strings.magicNumbers["total_high"]!! -> lifeFlowData["50_above"] ?: ""
            total > strings.magicNumbers["total_medium"]!! -> lifeFlowData["30_above"] ?: ""
            total > strings.magicNumbers["total_low"]!! -> lifeFlowData["20_above"] ?: ""
            else -> lifeFlowData["20_below"] ?: ""
        }
    }

    private fun getMeaning(stroke: Int): StrokeMeaningData {
        val moduloBase = strings.magicNumbers["modulo_base"]!!
        val meaning = strokeMeanings[stroke.toString()]
            ?: strokeMeanings[(stroke % moduloBase).toString()]
            ?: StrokeMeaningData(
                number = stroke,
                title = strings.defaultMeaning["title"] as String,
                summary = strings.defaultMeaning["summary"] as String,
                detailedExplanation = strings.defaultMeaning["detailed_explanation"] as String,
                positiveAspects = strings.defaultMeaning["positive_aspects"] as String,
                cautionPoints = strings.defaultMeaning["caution_points"] as String,
                personalityTraits = @Suppress("UNCHECKED_CAST") (strings.defaultMeaning["personality_traits"] as List<String>),
                suitableCareer = @Suppress("UNCHECKED_CAST") (strings.defaultMeaning["suitable_career"] as List<String>),
                lifePeriodInfluence = strings.defaultMeaning["life_period_influence"] as String
            )

        return meaning
    }
}