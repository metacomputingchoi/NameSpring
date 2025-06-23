// model/application/service/report/builder/ReportBuilder.kt
package com.ssc.namespring.model.application.service.report.builder

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.NameScore
import com.ssc.namespring.model.domain.name.value.NameReport
import com.ssc.namespring.model.application.service.report.analyzer.core.*
import com.ssc.namespring.model.application.service.report.analyzer.evaluation.ScoreEvaluator

class ReportBuilder {
    private val sajuAnalyzer = SajuAnalyzer()
    private val strokeAnalyzer = StrokeAnalyzer()
    private val elementAnalyzer = ElementAnalyzer()
    private val yinYangAnalyzer = YinYangAnalyzer()
    private val scoreEvaluator = ScoreEvaluator()
    private val strings = com.ssc.namespring.model.application.service.report.data.ReportDataHolder.reportBuilderStrings

    fun build(result: Name, scores: NameScore): NameReport {
        return NameReport(
            summary = buildSummary(result, scores),
            sajuAnalysis = sajuAnalyzer.analyzeBasic(result),
            strokeAnalysis = strokeAnalyzer.analyzeBasic(result),
            elementHarmony = elementAnalyzer.analyzeHarmony(result),
            yinYangBalance = yinYangAnalyzer.analyzeBalance(result),
            pronunciationAnalysis = analyzePronunciation(result),
            overallEvaluation = scoreEvaluator.evaluate(scores),
            recommendations = scoreEvaluator.getRecommendations(scores)
        )
    }

    private fun buildSummary(result: Name, scores: NameScore): String {
        val fullName = "${result.surHangul}${result.combinedPronounciation}"
        val fullHanja = "${result.surHanja}${result.combinedHanja}"
        val grade = scoreEvaluator.getGrade(scores.total)

        return strings.summaryFormat
            .replace("{name}", fullName)
            .replace("{hanja}", fullHanja)
            .replace("{score}", scores.total.toString())
            .replace("{grade}", grade)
    }

    private fun analyzePronunciation(result: Name): String {
        val pronunciation = result.combinedPronounciation ?: return strings.pronunciationError
        val isNatural = result.filteringProcess.any {
            it.step == strings.hangulNaturalnessStep && it.passed
        }

        return if (isNatural) {
            strings.pronunciationNatural.replace("{pronunciation}", pronunciation)
        } else {
            strings.pronunciationUnnatural.replace("{pronunciation}", pronunciation)
        }
    }
}