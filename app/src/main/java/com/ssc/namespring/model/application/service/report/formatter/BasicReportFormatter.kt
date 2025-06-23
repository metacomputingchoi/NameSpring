// model/application/service/report/formatter/BasicReportFormatter.kt
package com.ssc.namespring.model.application.service.report.formatter

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.NameScore
import com.ssc.namespring.model.domain.name.value.NameReport
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class BasicReportFormatter {
    private val templates = ReportDataHolder.reportTemplatesData
    private val basicSections = ReportDataHolder.basicReportSectionsData
    private val strings = ReportDataHolder.basicFormatterStrings

    fun print(result: Name, report: NameReport, scores: NameScore) {
        printHeader(result)
        printSummary(report)
        printScoreDetails(scores)
        printAnalysisDetails(report)
        printHanjaDetails(result)
        printFooter()
    }

    private fun printHeader(result: Name) {
        val separatorLength = strings.separatorLength
        val title = strings.title

        println(strings.separatorChar.repeat(separatorLength))
        println(strings.headerFormat
            .replace("{name}", "${result.surHangul}${result.combinedPronounciation}")
            .replace("{hanja}", "${result.surHanja}${result.combinedHanja}")
            .replace("{title}", title))
        println(strings.separatorChar.repeat(separatorLength))
    }

    private fun printSummary(report: NameReport) {
        println(strings.sectionPrefixes["summary"] + report.summary)
    }

    private fun printScoreDetails(scores: NameScore) {
        val categories = basicSections.scoreCategories

        println(strings.sectionPrefixes["score"])

        categories.forEach { category ->
            val score = when (category.field) {
                strings.scoreFields["fourTypesLuck"] -> scores.fourTypesLuck
                strings.scoreFields["nameElementHarmony"] -> scores.nameElementHarmony
                strings.scoreFields["typeElementHarmony"] -> scores.typeElementHarmony
                strings.scoreFields["yinYangBalance"] -> scores.yinYangBalance
                strings.scoreFields["sajuComplement"] -> scores.sajuComplement
                strings.scoreFields["pronunciation"] -> scores.pronunciation
                strings.scoreFields["meaning"] -> scores.meaning
                else -> 0
            }
            println(strings.scoreFormat
                .replace("{category}", category.name)
                .replace("{score}", score.toString())
                .replace("{max}", category.maxScore.toString()))
        }

        println(strings.totalSeparator)
        println(strings.totalFormat
            .replace("{score}", scores.total.toString())
            .replace("{max}", ReportDataHolder.reportBuilderStrings.totalMaxScore.toString()))
    }

    private fun printAnalysisDetails(report: NameReport) {
        val sections = basicSections.sections

        printSection(sections[0], report.sajuAnalysis)
        printSection(sections[1], report.strokeAnalysis)
        printSection(sections[2], report.elementHarmony)
        printSection(sections[3], report.yinYangBalance)
        printSection(sections[4], report.pronunciationAnalysis)
        printSection(sections[5], report.overallEvaluation)
        printRecommendations(report.recommendations)
    }

    private fun printSection(title: String, content: String) {
        println(strings.sectionPrefixes["section"] + title + strings.sectionSuffix)
        println(content)
    }

    private fun printRecommendations(recommendations: List<String>) {
        println(strings.sectionPrefixes["section"] + basicSections.sections[6] + strings.sectionSuffix)
        recommendations.forEach { println(strings.recommendationPrefix + it) }
    }

    private fun printHanjaDetails(result: Name) {
        println(strings.sectionPrefixes["hanja"] + basicSections.sections[7] + strings.sectionSuffix)
        printHanjaInfo(result.hanja1Info)
        printHanjaInfo(result.hanja2Info)
    }

    private fun printHanjaInfo(hanja: com.ssc.namespring.model.domain.hanja.entity.Hanja) {
        println(strings.hanjaInfoFormat
            .replace("{hanja}", hanja.hanja)
            .replace("{reading}", hanja.inmyeongYongEum ?: "")
            .replace("{meaning}", hanja.inmyeongYongDdeut ?: ""))
        println(strings.hanjaDetails["element"]!!
            .replace("{jawon}", hanja.jawonOheng ?: "")
            .replace("{baleum}", hanja.baleumOheng ?: ""))
        hanja.cautionRed?.let {
            println(strings.hanjaDetails["caution_red"]!!.replace("{caution}", it))
        }
        hanja.cautionBlue?.let {
            println(strings.hanjaDetails["caution_blue"]!!.replace("{caution}", it))
        }
    }

    private fun printFooter() {
        val separatorLength = strings.separatorLength
        println(strings.separatorChar.repeat(separatorLength))
    }
}