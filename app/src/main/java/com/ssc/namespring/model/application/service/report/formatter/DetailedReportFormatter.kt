// model/application/service/report/formatter/DetailedReportFormatter.kt
package com.ssc.namespring.model.application.service.report.formatter

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.*
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class DetailedReportFormatter {
    private val templates = ReportDataHolder.reportTemplatesData
    private val formatSettings = ReportDataHolder.formatSettingsData
    private val strings = ReportDataHolder.detailedFormatterStrings

    fun print(result: Name, report: DetailedNameReport, scores: NameScore) {
        printHeader(result)
        printBasicInfo(report.basicInfo)
        printSajuDetail(report.sajuDetail)
        printStrokeDetail(report.strokeDetail)
        printElementDetail(report.elementDetail)
        printYinYangDetail(report.yinYangDetail)
        printCharacterAnalysis(report.characterAnalysis)
        printFortunePrediction(report.fortunePrediction)
        printPersonalityAnalysis(report.personalityAnalysis)
        printCareerGuidance(report.careerGuidance)
        printComprehensiveAdvice(report.comprehensiveAdvice)
        printFooter()
    }

    private fun printHeader(result: Name) {
        val separatorLength = strings.separatorLength
        val title = strings.title

        println(strings.separators["main"]?.repeat(separatorLength))
        println(strings.headerFormat
            .replace("{name}", "${result.surHangul}${result.combinedPronounciation}")
            .replace("{hanja}", "${result.surHanja}${result.combinedHanja}")
            .replace("{title}", title))
        println(strings.separators["main"]?.repeat(separatorLength))
    }

    private fun printBasicInfo(info: BasicInfo) {
        val sectionNum = formatSettings.sectionNumbers["basic_info"] ?: "1"
        val sectionTitle = templates.sectionTitles["basic_info"] ?: ""

        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))
        println("${strings.prefixes["section"]} $sectionNum. $sectionTitle")
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))

        println(strings.infoFormats["name"]!!
            .replace("{prefix}", strings.prefixes["subsection"]!!)
            .replace("{label}", templates.subsectionLabels["name"] ?: "")
            .replace("{fullName}", info.fullName)
            .replace("{fullHanja}", info.fullHanja))

        println(strings.infoFormats["score"]!!
            .replace("{prefix}", strings.prefixes["subsection"]!!)
            .replace("{label}", templates.subsectionLabels["total_score"] ?: "")
            .replace("{score}", info.totalScore.toString()))

        println(strings.infoFormats["grade"]!!
            .replace("{prefix}", strings.prefixes["subsection"]!!)
            .replace("{label}", templates.subsectionLabels["grade"] ?: "")
            .replace("{grade}", info.grade))

        println("\n${strings.prefixes["subsection"]} ${templates.subsectionLabels["score_breakdown"]}:")

        val progressBar = strings.progressBar
        val barLength = (progressBar["length"] as? Number)?.toInt() ?: 20
        val barUnit = (progressBar["unit"] as? Number)?.toInt() ?: 5
        val filledBar = progressBar["filled"] as? String ?: "█"
        val emptyBar = progressBar["empty"] as? String ?: "□"

        info.scoreBreakdown.forEach { (category, score) ->
            val (current, max) = score
            val percentage = (current.toDouble() / max * strings.magicNumbers["max_score"]!!).toInt()
            val filledBars = percentage / barUnit
            val emptyBars = barLength - filledBars
            val bar = filledBar.repeat(filledBars) + emptyBar.repeat(emptyBars)
            println(progressBar["format"].toString()
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{category}", category)
                .replace("{current}", current.toString())
                .replace("{max}", max.toString())
                .replace("{bar}", bar)
                .replace("{percentage}", percentage.toString()))
        }
    }

    private fun printSajuDetail(detail: SajuDetail) {
        val sectionNum = formatSettings.sectionNumbers["saju_analysis"] ?: "2"
        val sectionTitle = templates.sectionTitles["saju_analysis"] ?: ""

        println()
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))
        println("${strings.prefixes["section"]} $sectionNum. $sectionTitle")
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))

        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["saju_composition"]}:")
        detail.fourPillars.forEach { (pillar, interpretation) ->
            println(strings.sajuFormats["pillar"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{name}", pillar)
                .replace("{interpretation}", interpretation))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["element_distribution"]}:")

        val dotDisplay = strings.dotDisplay
        val maxDots = (dotDisplay["max"] as? Number)?.toInt() ?: 8
        val filledDot = dotDisplay["filled"] as? String ?: "●"
        val emptyDot = dotDisplay["empty"] as? String ?: "○"

        detail.elementDistribution.forEach { (element, count) ->
            val filledDots = minOf(count, maxDots)
            val emptyDots = maxDots - filledDots
            val bar = filledDot.repeat(filledDots) + emptyDot.repeat(emptyDots)
            println(dotDisplay["format"].toString()
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{element}", element)
                .replace("{bar}", bar)
                .replace("{count}", count.toString()))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["dominant_element"]}: ${detail.dominantElement}")
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["lacking_elements"]}: ${detail.lackingElements.joinToString(strings.listFormats["joined"]!!)}")
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["element_balance"]}: ${detail.elementBalance}")
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["seasonal_influence"]}: ${detail.seasonalInfluence}")
        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["day_master_analysis"]}:")
        println(detail.dayMasterAnalysis)
    }

    private fun printStrokeDetail(detail: StrokeDetail) {
        val sectionNum = formatSettings.sectionNumbers["stroke_analysis"] ?: "3"
        val sectionTitle = templates.sectionTitles["stroke_analysis"] ?: ""

        println()
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))
        println("${strings.prefixes["section"]} $sectionNum. $sectionTitle")
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))

        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["stroke_numbers"]}:")
        detail.strokeNumbers.forEach { (name, stroke) ->
            println(strings.strokeFormats["number"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{name}", name)
                .replace("{stroke}", stroke.toString()))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["luck_analysis"]}:")
        detail.luckAnalysis.forEach { (name, analysis) ->
            println(strings.strokeFormats["luck"]!!
                .replace("{prefix}", strings.prefixes["subitem"]!!)
                .replace("{name}", name))
            analysis.lines().forEach { line ->
                println(strings.sajuFormats["analysis"]!!.replace("{line}", line))
            }
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["numerology_meaning"]}:")
        detail.numerologyMeaning.forEach { (name, meaning) ->
            println(strings.strokeFormats["meaning"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{name}", name)
                .replace("{meaning}", meaning))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["life_path_influence"]}:")
        println(detail.lifePathInfluence)
    }

    private fun printElementDetail(detail: ElementDetail) {
        val sectionNum = formatSettings.sectionNumbers["element_analysis"] ?: "4"
        val sectionTitle = templates.sectionTitles["element_analysis"] ?: ""

        println()
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))
        println("${strings.prefixes["section"]} $sectionNum. $sectionTitle")
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))

        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["name_elements"]}: ${detail.nameElements.joinToString(strings.listFormats["joined"]!!)}")

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["element_relationships"]}:")
        detail.relationshipMap.forEach { relation ->
            println(strings.elementFormats["relation"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{from}", relation.from)
                .replace("{to}", relation.to)
                .replace("{type}", relation.type))
            println(strings.elementFormats["influence"]!!
                .replace("{prefix}", strings.prefixes["tree"]!!)
                .replace("{influence}", relation.influence))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["cycle_analysis"]}:")
        println(detail.cycleAnalysis)

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["strength_analysis"]}:")
        detail.strengthAnalysis.forEach { (position, strength) ->
            println(strings.elementFormats["strength"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{position}", position)
                .replace("{strength}", strength))
        }
    }

    private fun printYinYangDetail(detail: YinYangDetail) {
        val sectionNum = formatSettings.sectionNumbers["yin_yang_analysis"] ?: "5"
        val sectionTitle = templates.sectionTitles["yin_yang_analysis"] ?: ""

        println()
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))
        println("${strings.prefixes["section"]} $sectionNum. $sectionTitle")
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))

        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["yin_yang_pattern"]}: ${detail.pattern}")
        println(strings.yinyangFormats["distribution"]!!
            .replace("{prefix}", strings.prefixes["subsection"]!!)
            .replace("{label}", templates.subsectionLabels["yin_yang_distribution"] ?: "")
            .replace("{yin}", (detail.distribution["음"] ?: 0).toString())
            .replace("{yang}", (detail.distribution["양"] ?: 0).toString()))
        println(strings.yinyangFormats["score"]!!
            .replace("{prefix}", strings.prefixes["subsection"]!!)
            .replace("{label}", templates.subsectionLabels["balance_score"] ?: "")
            .replace("{score}", detail.balanceScore.toString()))
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["energy_type"]}: ${detail.energyType}")
        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["energy_flow"]}:")
        println(detail.flowAnalysis)
    }

    private fun printCharacterAnalysis(analysis: CharacterAnalysis) {
        val sectionNum = formatSettings.sectionNumbers["character_analysis"] ?: "6"
        val sectionTitle = templates.sectionTitles["character_analysis"] ?: ""

        println()
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))
        println("${strings.prefixes["section"]} $sectionNum. $sectionTitle")
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))

        analysis.hanjaDetails.forEach { hanja ->
            println(strings.characterFormats["hanja"]!!
                .replace("{prefix}", strings.prefixes["subsection"]!!)
                .replace("{character}", hanja.character)
                .replace("{meaning}", hanja.meaning))
            println(strings.characterFormats["origin"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{origin}", hanja.origin))
            println(strings.characterFormats["components"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{components}", hanja.components.joinToString(strings.listFormats["joined"]!!)))
            println(strings.characterFormats["related"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{related}", hanja.relatedCharacters.joinToString(strings.listFormats["joined"]!!)))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["combined_meaning"]}: ${analysis.combinedMeaning}")
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["symbolic_interpretation"]}: ${analysis.symbolicInterpretation}")
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["cultural_significance"]}: ${analysis.culturalSignificance}")
    }

    private fun printFortunePrediction(fortune: FortunePrediction) {
        val sectionNum = formatSettings.sectionNumbers["fortune_prediction"] ?: "7"
        val sectionTitle = templates.sectionTitles["fortune_prediction"] ?: ""

        println()
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))
        println("${strings.prefixes["section"]} $sectionNum. $sectionTitle")
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))

        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["overall_fortune"]}: ${fortune.overallFortune}")

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["life_periods"]}:")
        fortune.lifePeriods.forEach { (period, fortuneDetail) ->
            println(strings.listFormats["item"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{item}", "$period: $fortuneDetail"))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["lucky_elements"]}: ${fortune.luckyElements.joinToString(strings.listFormats["joined"]!!)}")
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["challenge_periods"]}: ${fortune.challengePeriods.joinToString(strings.listFormats["joined"]!!)}")
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["opportunity_areas"]}: ${fortune.opportunityAreas.joinToString(strings.listFormats["joined"]!!)}")
    }

    private fun printPersonalityAnalysis(personality: PersonalityAnalysis) {
        val sectionNum = formatSettings.sectionNumbers["personality_analysis"] ?: "8"
        val sectionTitle = templates.sectionTitles["personality_analysis"] ?: ""

        println()
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))
        println("${strings.prefixes["section"]} $sectionNum. $sectionTitle")
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))

        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["core_traits"]}: ${personality.coreTraits.joinToString(strings.listFormats["joined"]!!)}")

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["strengths"]}:")
        personality.strengths.forEach { strength ->
            println(strings.listFormats["item"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{item}", strength))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["weaknesses"]}:")
        personality.weaknesses.forEach { weakness ->
            println(strings.listFormats["item"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{item}", weakness))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["emotional_tendency"]}: ${personality.emotionalTendency}")
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["social_style"]}: ${personality.socialStyle}")
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["leadership_potential"]}: ${personality.leadershipPotential}")
    }

    private fun printCareerGuidance(career: CareerGuidance) {
        val sectionNum = formatSettings.sectionNumbers["career_guidance"] ?: "9"
        val sectionTitle = templates.sectionTitles["career_guidance"] ?: ""

        println()
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))
        println("${strings.prefixes["section"]} $sectionNum. $sectionTitle")
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))

        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["suitable_fields"]}:")
        career.suitableFields.forEach { field ->
            println(strings.listFormats["item"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{item}", field))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["avoid_fields"]}:")
        career.avoidFields.forEach { field ->
            println(strings.listFormats["item"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{item}", field))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["work_style"]}: ${career.workStyle}")
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["success_factors"]}: ${career.successFactors.joinToString(strings.listFormats["joined"]!!)}")
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["business_luck"]}: ${career.businessLuck}")
    }

    private fun printComprehensiveAdvice(advice: ComprehensiveAdvice) {
        val sectionNum = formatSettings.sectionNumbers["comprehensive_advice"] ?: "10"
        val sectionTitle = templates.sectionTitles["comprehensive_advice"] ?: ""

        println()
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))
        println("${strings.prefixes["section"]} $sectionNum. $sectionTitle")
        println(strings.separators["section"]?.repeat(strings.sectionSeparatorLength))

        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["summary"]}:")
        println(advice.summary)

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["key_recommendations"]}:")
        advice.keyRecommendations.forEach { recommendation ->
            println(strings.listFormats["item"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{item}", recommendation))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["caution_points"]}:")
        advice.cautionPoints.forEach { point ->
            println(strings.listFormats["item"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{item}", point))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["development_areas"]}:")
        advice.developmentAreas.forEach { area ->
            println(strings.listFormats["item"]!!
                .replace("{prefix}", strings.prefixes["item"]!!)
                .replace("{item}", area))
        }

        println()
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["lucky_colors"]}: ${advice.luckyColors.joinToString(strings.listFormats["joined"]!!)}")
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["lucky_numbers"]}: ${advice.luckyNumbers.joinToString(strings.listFormats["joined"]!!)}")
        println("${strings.prefixes["subsection"]} ${templates.subsectionLabels["compatible_names"]}: ${advice.compatibleNames.joinToString(strings.listFormats["joined"]!!)}")
    }

    private fun printFooter() {
        val footerMessages = templates.footerMessages
        val separatorLength = strings.separatorLength

        println()
        println(strings.separators["main"]?.repeat(separatorLength))
        println("${strings.footerDisclaimerPrefix} ${footerMessages["disclaimer1"] ?: ""}")
        println("${strings.footerDisclaimerPrefix} ${footerMessages["disclaimer2"] ?: ""}")
        println(strings.separators["main"]?.repeat(separatorLength))
        println()
    }
}