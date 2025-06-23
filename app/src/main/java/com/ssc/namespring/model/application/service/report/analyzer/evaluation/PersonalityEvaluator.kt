// model/application/service/report/analyzer/evaluation/PersonalityEvaluator.kt
package com.ssc.namespring.model.application.service.report.analyzer.evaluation

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.NameScore
import com.ssc.namespring.model.domain.name.value.PersonalityAnalysis
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class PersonalityEvaluator {
    private val strokeMeanings = ReportDataHolder.strokeMeaningsData.strokeMeanings
    private val elementData = ReportDataHolder.elementCharacteristicsData
    private val personalityTraitsData = ReportDataHolder.personalityTraitsData
    private val yinYangData = ReportDataHolder.yinYangPatternsData
    private val businessLuckData = ReportDataHolder.businessLuckData
    private val strings = ReportDataHolder.personalityEvaluatorStrings

    fun analyze(result: Name, scores: NameScore): PersonalityAnalysis {
        val strokes = result.combinationAnalysis.fourTypes

        return PersonalityAnalysis(
            coreTraits = analyzeCoreTraits(strokes),
            strengths = analyzeStrengths(strokes, result),
            weaknesses = analyzeWeaknesses(strokes, result),
            emotionalTendency = analyzeEmotionalTendency(result),
            socialStyle = analyzeSocialStyle(strokes[strings.socialIndex]), // 이격
            leadershipPotential = analyzeLeadershipPotential(strokes)
        )
    }

    fun getCautionPoints(result: Name): List<String> {
        val cautionPoints = mutableListOf<String>()
        val strokes = result.combinationAnalysis.fourTypes

        strokes.forEach { stroke ->
            val meaning = strokeMeanings[stroke.toString()]
            if (!meaning?.cautionPoints.isNullOrEmpty()) {
                cautionPoints.add(meaning!!.cautionPoints)
            }
        }

        return cautionPoints.distinct()
    }

    fun getDevelopmentAreas(result: Name): List<String> {
        val areas = mutableListOf<String>()
        val developmentAreas = elementData.elementDevelopmentAreas

        // 오행 부족에 따른 개발 영역
        result.zeroElements.forEach { element ->
            developmentAreas[element]?.let { areas.add(it) }
        }

        return areas
    }

    private fun analyzeCoreTraits(strokes: List<Int>): List<String> {
        val traits = mutableListOf<String>()

        // 원격(주격) 기반 핵심 성격
        val personalityStroke = strokes[0]
        val meaning = strokeMeanings[personalityStroke.toString()]

        meaning?.personalityTraits?.let { traits.addAll(it) }

        return traits.take(strings.thresholds["top_traits"]!!) // 상위 N개만
    }

    private fun analyzeStrengths(strokes: List<Int>, result: Name): List<String> {
        val strengths = mutableListOf<String>()

        strokes.forEach { stroke ->
            val meaning = strokeMeanings[stroke.toString()]
            meaning?.personalityTraits?.filter {
                it.contains(ReportDataHolder.careerEvaluatorStrings.personalityKeywords["ability"]!!) ||
                        it.contains(ReportDataHolder.careerEvaluatorStrings.personalityKeywords["talent"]!!)
            }?.let { strengths.addAll(it) }
        }

        return strengths.distinct().take(strings.thresholds["top_strengths"]!!)
    }

    private fun analyzeWeaknesses(strokes: List<Int>, result: Name): List<String> {
        val weaknesses = mutableListOf<String>()

        strokes.forEach { stroke ->
            val meaning = strokeMeanings[stroke.toString()]
            if (!meaning?.cautionPoints.isNullOrEmpty()) {
                weaknesses.add(transformToWeakness(meaning.cautionPoints))
            }
        }

        return weaknesses.distinct().take(strings.thresholds["top_weaknesses"]!!)
    }

    private fun transformToWeakness(cautionPoint: String): String {
        val transformations = personalityTraitsData.weaknessTransformations

        for ((key, value) in transformations) {
            if (cautionPoint.contains(key)) {
                return value
            }
        }

        return cautionPoint
    }

    private fun analyzeEmotionalTendency(result: Name): String {
        val pmPattern = result.combinedPm ?: ReportDataHolder.yinYangAnalyzerStrings.defaultPattern
        val yinCount = pmPattern.count { it == ReportDataHolder.yinYangAnalyzerStrings.yinChar[0] }

        val emotionalTendencies = yinYangData.emotionalTendencies

        return when {
            yinCount >= strings.thresholds["yin_dominant"]!! -> emotionalTendencies["yin_dominant"] ?: ""
            yinCount == strings.thresholds["yang_dominant"]!! -> emotionalTendencies["yang_dominant"] ?: ""
            else -> emotionalTendencies["balanced"] ?: ""
        }
    }

    private fun analyzeSocialStyle(socialStroke: Int): String {
        val meaning = strokeMeanings[socialStroke.toString()]
        val socialStyles = personalityTraitsData.socialStyles
        val keywords = ReportDataHolder.careerEvaluatorStrings.workStyleKeywords

        return when {
            meaning?.summary?.contains(keywords["leadership"]!!) == true -> socialStyles["leader"] ?: ""
            meaning?.summary?.contains(keywords["teamwork"]!!) == true -> socialStyles["cooperative"] ?: ""
            meaning?.summary?.contains(keywords["independent"]!!) == true -> socialStyles["independent"] ?: ""
            else -> socialStyles["flexible"] ?: ""
        }
    }

    private fun analyzeLeadershipPotential(strokes: List<Int>): String {
        val leadershipCount = strokes.count { it in businessLuckData.leadershipStrokes }
        val evaluations = businessLuckData.leadershipEvaluations
        val thresholds = ReportDataHolder.careerEvaluatorStrings.thresholds

        return when {
            leadershipCount >= thresholds["leadership_excellent"]!! -> evaluations["3_above"] ?: ""
            leadershipCount >= thresholds["leadership_good"]!! -> evaluations["2_above"] ?: ""
            leadershipCount >= thresholds["leadership_fair"]!! -> evaluations["1_above"] ?: ""
            else -> evaluations["0"] ?: ""
        }
    }
}