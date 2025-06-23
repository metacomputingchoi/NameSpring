// model/application/service/report/analyzer/evaluation/CareerEvaluator.kt
package com.ssc.namespring.model.application.service.report.analyzer.evaluation

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.NameScore
import com.ssc.namespring.model.domain.name.value.CareerGuidance
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class CareerEvaluator {
    private val strokeMeanings = ReportDataHolder.strokeMeaningsData.strokeMeanings
    private val elementData = ReportDataHolder.elementCharacteristicsData
    private val businessLuckData = ReportDataHolder.businessLuckData
    private val careerFieldsData = ReportDataHolder.careerFieldsData
    private val personalityTraitsData = ReportDataHolder.personalityTraitsData
    private val strings = ReportDataHolder.careerEvaluatorStrings

    fun guide(result: Name, scores: NameScore): CareerGuidance {
        val strokes = result.combinationAnalysis.fourTypes
        val elements = result.combinedElement ?: ""

        return CareerGuidance(
            suitableFields = findSuitableFields(strokes, elements),
            avoidFields = findAvoidFields(strokes),
            workStyle = analyzeWorkStyle(strokes, result),
            successFactors = findSuccessFactors(result, scores),
            businessLuck = analyzeBusinessLuck(strokes, scores)
        )
    }

    private fun findSuitableFields(strokes: List<Int>, elements: String): List<String> {
        val fields = mutableListOf<String>()

        // 수리별 적합 직종
        strokes.forEach { stroke ->
            val meaning = strokeMeanings[stroke.toString()]
            meaning?.suitableCareer?.let { fields.addAll(it) }
        }

        // 오행별 적합 직종
        val careerFields = elementData.elementCareerFields
        elements.forEach { element ->
            careerFields[element.toString()]?.let { fields.add(it) }
        }

        return fields.distinct()
    }

    private fun findAvoidFields(strokes: List<Int>): List<String> {
        val avoidFields = mutableListOf<String>()
        val avoidanceStrokes = careerFieldsData.avoidanceStrokes

        if (strokes.any { it in avoidanceStrokes.highRisk }) {
            avoidFields.add(avoidanceStrokes.avoidMessage)
        }

        if (strokes.any { it in avoidanceStrokes.stabilityUnsuitable }) {
            avoidFields.add(avoidanceStrokes.stabilityMessage)
        }

        return avoidFields
    }

    private fun analyzeWorkStyle(strokes: List<Int>, result: Name): String {
        val personalityStroke = strokes[0]
        val meaning = strokeMeanings[personalityStroke.toString()]
        val workStyles = personalityTraitsData.workStyles

        return when {
            meaning?.summary?.contains(strings.workStyleKeywords["independent"]!!) == true -> workStyles["independent"] ?: ""
            meaning?.summary?.contains(strings.workStyleKeywords["teamwork"]!!) == true -> workStyles["teamwork"] ?: ""
            meaning?.summary?.contains(strings.workStyleKeywords["leadership"]!!) == true -> workStyles["leadership"] ?: ""
            else -> workStyles["adaptive"] ?: ""
        }
    }

    private fun findSuccessFactors(result: Name, scores: NameScore): List<String> {
        val factors = mutableListOf<String>()

        if (scores.fourTypesLuck >= strings.thresholds["high_luck"]!!) {
            factors.add(strings.successFactors["high_luck"]!!)
        }

        if (scores.nameElementHarmony >= strings.thresholds["high_harmony"]!!) {
            factors.add(strings.successFactors["harmony"]!!)
        }

        if (scores.yinYangBalance >= strings.thresholds["high_balance"]!!) {
            factors.add(strings.successFactors["balance"]!!)
        }

        // 부족한 부분을 보완하는 노력도 성공 요인
        val lackingRecommendations = elementData.elementLackingRecommendations
        result.zeroElements.forEach { element ->
            lackingRecommendations[element]?.let { factors.add(it) }
        }

        return factors
    }

    private fun analyzeBusinessLuck(strokes: List<Int>, scores: NameScore): String {
        val businessCount = strokes.count { it in businessLuckData.businessLuckStrokes }
        val evaluations = businessLuckData.businessEvaluations

        return when {
            businessCount >= strings.thresholds["business_excellent"]!! && scores.total >= strings.thresholds["total_good"]!! -> evaluations["3_above"] ?: ""
            businessCount >= strings.thresholds["business_good"]!! -> evaluations["2_above"] ?: ""
            businessCount >= strings.thresholds["business_fair"]!! -> evaluations["1_above"] ?: ""
            else -> evaluations["0"] ?: ""
        }
    }
}