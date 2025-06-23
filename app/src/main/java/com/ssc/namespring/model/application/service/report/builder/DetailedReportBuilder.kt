// model/application/service/report/builder/DetailedReportBuilder.kt
package com.ssc.namespring.model.application.service.report.builder

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.*
import com.ssc.namespring.model.application.service.report.analyzer.core.*
import com.ssc.namespring.model.application.service.report.analyzer.meaning.*
import com.ssc.namespring.model.application.service.report.analyzer.evaluation.*

class DetailedReportBuilder {
    private val sajuAnalyzer = SajuAnalyzer()
    private val strokeAnalyzer = StrokeAnalyzer()
    private val elementAnalyzer = ElementAnalyzer()
    private val yinYangAnalyzer = YinYangAnalyzer()
    private val strokeMeaningAnalyzer = StrokeMeaningAnalyzer()
    private val characterMeaningAnalyzer = CharacterMeaningAnalyzer()
    private val personalityEvaluator = PersonalityEvaluator()
    private val fortuneEvaluator = FortuneEvaluator()
    private val careerEvaluator = CareerEvaluator()
    private val scoreEvaluator = ScoreEvaluator()
    private val strings = com.ssc.namespring.model.application.service.report.data.ReportDataHolder.reportBuilderStrings

    fun build(result: Name, scores: NameScore): DetailedNameReport {
        return DetailedNameReport(
            basicInfo = buildBasicInfo(result, scores),
            sajuDetail = sajuAnalyzer.analyzeDetailed(result),
            strokeDetail = strokeAnalyzer.analyzeDetailed(result),
            elementDetail = elementAnalyzer.analyzeDetailed(result),
            yinYangDetail = yinYangAnalyzer.analyzeDetailed(result),
            characterAnalysis = characterMeaningAnalyzer.analyze(result),
            fortunePrediction = fortuneEvaluator.predict(result, scores),
            personalityAnalysis = personalityEvaluator.analyze(result, scores),
            careerGuidance = careerEvaluator.guide(result, scores),
            comprehensiveAdvice = buildComprehensiveAdvice(result, scores)
        )
    }

    private fun buildBasicInfo(result: Name, scores: NameScore): BasicInfo {
        val fullName = "${result.surHangul}${result.combinedPronounciation}"
        val fullHanja = "${result.surHanja}${result.combinedHanja}"

        return BasicInfo(
            fullName = fullName,
            fullHanja = fullHanja,
            totalScore = scores.total,
            grade = scoreEvaluator.getGrade(scores.total),
            scoreBreakdown = mapOf(
                strings.scoreCategories["four_types"]!! to (scores.fourTypesLuck to strings.maxScores["four_types"]!!),
                strings.scoreCategories["name_element"]!! to (scores.nameElementHarmony to strings.maxScores["name_element"]!!),
                strings.scoreCategories["type_element"]!! to (scores.typeElementHarmony to strings.maxScores["type_element"]!!),
                strings.scoreCategories["yin_yang"]!! to (scores.yinYangBalance to strings.maxScores["yin_yang"]!!),
                strings.scoreCategories["saju"]!! to (scores.sajuComplement to strings.maxScores["saju"]!!),
                strings.scoreCategories["pronunciation"]!! to (scores.pronunciation to strings.maxScores["pronunciation"]!!),
                strings.scoreCategories["meaning"]!! to (scores.meaning to strings.maxScores["meaning"]!!)
            )
        )
    }

    private fun buildComprehensiveAdvice(result: Name, scores: NameScore): ComprehensiveAdvice {
        val strokeMeaning = strokeMeaningAnalyzer.getComprehensiveMeaning(result)

        return ComprehensiveAdvice(
            summary = strings.comprehensivePrefix + scoreEvaluator.getOverallAssessment(scores),
            keyRecommendations = scoreEvaluator.getDetailedRecommendations(result, scores),
            cautionPoints = personalityEvaluator.getCautionPoints(result),
            developmentAreas = personalityEvaluator.getDevelopmentAreas(result),
            luckyColors = elementAnalyzer.getLuckyColors(result),
            luckyNumbers = strokeAnalyzer.getLuckyNumbers(result),
            compatibleNames = fortuneEvaluator.getCompatibleNames(result)
        )
    }
}