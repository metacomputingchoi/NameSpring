// model/application/service/report/analyzer/evaluation/FortuneEvaluator.kt
package com.ssc.namespring.model.application.service.report.analyzer.evaluation

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.NameScore
import com.ssc.namespring.model.domain.name.value.FortunePrediction
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class FortuneEvaluator {
    private val strokeMeanings = ReportDataHolder.strokeMeaningsData.strokeMeanings
    private val scoreEvaluationsData = ReportDataHolder.scoreEvaluationsData
    private val lifePeriodsData = ReportDataHolder.lifePeriodsData
    private val strings = ReportDataHolder.fortuneEvaluatorStrings

    fun predict(result: Name, scores: NameScore): FortunePrediction {
        val strokes = result.combinationAnalysis.fourTypes

        return FortunePrediction(
            overallFortune = predictOverallFortune(scores),
            lifePeriods = predictLifePeriods(strokes),
            luckyElements = findLuckyElements(result),
            challengePeriods = findChallengePeriods(strokes),
            opportunityAreas = findOpportunityAreas(result, strokes)
        )
    }

    fun getCompatibleNames(result: Name): List<String> {
        val elements = result.combinedElement ?: ""
        val compatibleElements = getCompatibleElements(elements)

        // 실제로는 데이터베이스에서 호환되는 이름을 찾아야 함
        return strings.exampleNames
    }

    private fun predictOverallFortune(scores: NameScore): String {
        val evaluations = scoreEvaluationsData.overallEvaluations

        return when {
            scores.total >= strings.thresholds["overall_excellent"]!! -> evaluations["80_above"] ?: ""
            scores.total >= strings.thresholds["overall_good"]!! -> evaluations["70_above"] ?: ""
            scores.total >= strings.thresholds["overall_fair"]!! -> evaluations["60_above"] ?: ""
            else -> evaluations["60_below"] ?: ""
        }
    }

    private fun predictLifePeriods(strokes: List<Int>): Map<String, String> {
        val periods = mutableMapOf<String, String>()
        val periodNames = lifePeriodsData.lifePeriodNames
        val periodMapping = lifePeriodsData.lifePeriodMapping

        // 형격 - 초년운 (1-30세)
        val foundationMeaning = strokeMeanings[strokes[1].toString()]
        periods[periodNames["초년"] ?: strings.lifePeriods["early"]!!] =
            strings.periodInfluenceFormat
                .replace("{type}", periodMapping["형격"] ?: "형격")
                .replace("{stroke}", strokes[1].toString())
                .replace("{influence}", foundationMeaning?.lifePeriodInfluence ?: "")

        // 원격 - 중년운 (31-50세)
        val personalityMeaning = strokeMeanings[strokes[0].toString()]
        periods[periodNames["중년"] ?: strings.lifePeriods["middle"]!!] =
            strings.periodInfluenceFormat
                .replace("{type}", periodMapping["원격"] ?: "원격")
                .replace("{stroke}", strokes[0].toString())
                .replace("{influence}", personalityMeaning?.lifePeriodInfluence ?: "")

        // 총격 - 말년운 (51세 이후)
        val totalMeaning = strokeMeanings[strokes[3].toString()]
        periods[periodNames["말년"] ?: strings.lifePeriods["late"]!!] =
            strings.periodInfluenceFormat
                .replace("{type}", periodMapping["총격"] ?: "총격")
                .replace("{stroke}", strokes[3].toString())
                .replace("{influence}", totalMeaning?.lifePeriodInfluence ?: "")

        return periods
    }

    private fun findLuckyElements(result: Name): List<String> {
        val luckyElements = mutableListOf<String>()

        // 부족한 오행이 행운 요소가 됨
        result.zeroElements.forEach { element ->
            luckyElements.add(strings.luckyElementFormat.replace("{element}", element))
        }

        // 상생 오행도 행운 요소
        result.combinedElement?.forEach { char ->
            val nextElement = getNextGenerativeElement(char.toString())
            luckyElements.add(strings.energyFormat.replace("{element}", nextElement))
        }

        return luckyElements.distinct()
    }

    private fun findChallengePeriods(strokes: List<Int>): List<String> {
        val challenges = mutableListOf<String>()

        strokes.forEachIndexed { index, stroke ->
            val meaning = strokeMeanings[stroke.toString()]
            if (meaning?.challengePeriod != null) {
                val periodName = when (index) {
                    0 -> strings.periodNames["personality"]!!
                    1 -> strings.periodNames["foundation"]!!
                    2 -> strings.periodNames["social"]!!
                    3 -> strings.periodNames["total"]!!
                    else -> strings.periodNames["default"]!!
                }
                challenges.add(strings.challengeFormat
                    .replace("{period}", periodName)
                    .replace("{challenge}", meaning.challengePeriod))
            }
        }

        return challenges
    }

    private fun findOpportunityAreas(result: Name, strokes: List<Int>): List<String> {
        val opportunities = mutableListOf<String>()

        // 길수가 많은 분야
        if (result.combinationAnalysis.fourTypesLuck.sum() >= strings.thresholds["many_luck"]!!) {
            opportunities.add(strings.businessOpportunity)
        }

        // 특정 수리별 기회
        strokes.forEach { stroke ->
            val meaning = strokeMeanings[stroke.toString()]
            meaning?.opportunityArea?.let { opportunities.add(it) }
        }

        return opportunities.distinct()
    }

    private fun getCompatibleElements(elements: String): List<String> {
        // 상생 관계의 오행들을 찾음
        val compatible = mutableListOf<String>()

        elements.forEach { element ->
            compatible.add(getNextGenerativeElement(element.toString()))
        }

        return compatible
    }

    private fun getNextGenerativeElement(element: String): String {
        return strings.elementCycle[element] ?: element
    }
}