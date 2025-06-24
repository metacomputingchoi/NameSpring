// presentation/formatter/SimpleReportFormatter.kt
package com.ssc.namespring.presentation.formatter

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.*
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class SimpleReportFormatter : ReportFormatter {
    private val templates = ReportDataHolder.reportTemplatesData
    private val basicSections = ReportDataHolder.basicReportSectionsData
    private val formatSettings = ReportDataHolder.formatSettingsData
    private val basicStrings = ReportDataHolder.basicFormatterStrings
    private val detailedStrings = ReportDataHolder.detailedFormatterStrings

    override fun formatBasicReport(name: Name, report: NameReport, scores: NameScore): String {
        val sb = StringBuilder()

        // Header
        val separatorLength = basicStrings.separatorLength
        sb.appendLine(basicStrings.separatorChar.repeat(separatorLength))
        sb.appendLine(basicStrings.headerFormat
            .replace("{name}", "${name.surHangul}${name.combinedPronounciation}")
            .replace("{hanja}", "${name.surHanja}${name.combinedHanja}")
            .replace("{title}", basicStrings.title))
        sb.appendLine(basicStrings.separatorChar.repeat(separatorLength))

        // Summary
        sb.appendLine(basicStrings.sectionPrefixes["summary"] + report.summary)

        // Score Details
        sb.appendLine(basicStrings.sectionPrefixes["score"])

        val categories = basicSections.scoreCategories
        categories.forEach { category ->
            val score = when (category.field) {
                basicStrings.scoreFields["fourTypesLuck"] -> scores.fourTypesLuck
                basicStrings.scoreFields["nameElementHarmony"] -> scores.nameElementHarmony
                basicStrings.scoreFields["typeElementHarmony"] -> scores.typeElementHarmony
                basicStrings.scoreFields["yinYangBalance"] -> scores.yinYangBalance
                basicStrings.scoreFields["sajuComplement"] -> scores.sajuComplement
                basicStrings.scoreFields["pronunciation"] -> scores.pronunciation
                basicStrings.scoreFields["meaning"] -> scores.meaning
                else -> 0
            }
            sb.appendLine(basicStrings.scoreFormat
                .replace("{category}", category.name)
                .replace("{score}", score.toString())
                .replace("{max}", category.maxScore.toString()))
        }

        sb.appendLine(basicStrings.totalSeparator)
        sb.appendLine(basicStrings.totalFormat
            .replace("{score}", scores.total.toString())
            .replace("{max}", ReportDataHolder.reportBuilderStrings.totalMaxScore.toString()))

        // Analysis Details
        val sections = basicSections.sections
        appendSection(sb, sections[0], report.sajuAnalysis)
        appendSection(sb, sections[1], report.strokeAnalysis)
        appendSection(sb, sections[2], report.elementHarmony)
        appendSection(sb, sections[3], report.yinYangBalance)
        appendSection(sb, sections[4], report.pronunciationAnalysis)
        appendSection(sb, sections[5], report.overallEvaluation)

        // Recommendations
        sb.appendLine(basicStrings.sectionPrefixes["section"] + sections[6] + basicStrings.sectionSuffix)
        report.recommendations.forEach {
            sb.appendLine(basicStrings.recommendationPrefix + it)
        }

        // Hanja Details
        sb.appendLine(basicStrings.sectionPrefixes["hanja"] + sections[7] + basicStrings.sectionSuffix)
        appendHanjaInfo(sb, name.hanja1Info)
        appendHanjaInfo(sb, name.hanja2Info)

        // Footer
        sb.appendLine(basicStrings.separatorChar.repeat(separatorLength))

        return sb.toString()
    }

    override fun formatDetailedReport(name: Name, report: DetailedNameReport, scores: NameScore): String {
        val sb = StringBuilder()

        // Header
        val separatorLength = detailedStrings.separatorLength
        sb.appendLine(detailedStrings.separators["main"]?.repeat(separatorLength))
        sb.appendLine(detailedStrings.headerFormat
            .replace("{name}", "${name.surHangul}${name.combinedPronounciation}")
            .replace("{hanja}", "${name.surHanja}${name.combinedHanja}")
            .replace("{title}", detailedStrings.title))
        sb.appendLine(detailedStrings.separators["main"]?.repeat(separatorLength))

        appendBasicInfo(sb, report.basicInfo)
        appendSajuDetail(sb, report.sajuDetail)
        appendStrokeDetail(sb, report.strokeDetail)
        appendElementDetail(sb, report.elementDetail)
        appendYinYangDetail(sb, report.yinYangDetail)
        appendCharacterAnalysis(sb, report.characterAnalysis)
        appendFortunePrediction(sb, report.fortunePrediction)
        appendPersonalityAnalysis(sb, report.personalityAnalysis)
        appendCareerGuidance(sb, report.careerGuidance)
        appendComprehensiveAdvice(sb, report.comprehensiveAdvice)

        // Footer
        val footerMessages = templates.footerMessages
        sb.appendLine()
        sb.appendLine(detailedStrings.separators["main"]?.repeat(separatorLength))
        sb.appendLine("${detailedStrings.footerDisclaimerPrefix} ${footerMessages["disclaimer1"] ?: ""}")
        sb.appendLine("${detailedStrings.footerDisclaimerPrefix} ${footerMessages["disclaimer2"] ?: ""}")
        sb.appendLine(detailedStrings.separators["main"]?.repeat(separatorLength))
        sb.appendLine()

        return sb.toString()
    }

    private fun appendSection(sb: StringBuilder, title: String, content: String) {
        sb.appendLine(basicStrings.sectionPrefixes["section"] + title + basicStrings.sectionSuffix)
        sb.appendLine(content)
    }

    private fun appendHanjaInfo(sb: StringBuilder, hanja: com.ssc.namespring.model.domain.hanja.entity.Hanja) {
        sb.appendLine(basicStrings.hanjaInfoFormat
            .replace("{hanja}", hanja.hanja)
            .replace("{reading}", hanja.inmyeongYongEum ?: "")
            .replace("{meaning}", hanja.inmyeongYongDdeut ?: ""))
        sb.appendLine(basicStrings.hanjaDetails["element"]!!
            .replace("{jawon}", hanja.jawonOheng ?: "")
            .replace("{baleum}", hanja.baleumOheng ?: ""))
        hanja.cautionRed?.let {
            sb.appendLine(basicStrings.hanjaDetails["caution_red"]!!.replace("{caution}", it))
        }
        hanja.cautionBlue?.let {
            sb.appendLine(basicStrings.hanjaDetails["caution_blue"]!!.replace("{caution}", it))
        }
    }

    private fun appendBasicInfo(sb: StringBuilder, info: BasicInfo) {
        val sectionNum = formatSettings.sectionNumbers["basic_info"] ?: "1"
        val sectionTitle = templates.sectionTitles["basic_info"] ?: ""

        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))
        sb.appendLine("${detailedStrings.prefixes["section"]} $sectionNum. $sectionTitle")
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))

        sb.appendLine(detailedStrings.infoFormats["name"]!!
            .replace("{prefix}", detailedStrings.prefixes["subsection"]!!)
            .replace("{label}", templates.subsectionLabels["name"] ?: "")
            .replace("{fullName}", info.fullName)
            .replace("{fullHanja}", info.fullHanja))

        sb.appendLine(detailedStrings.infoFormats["score"]!!
            .replace("{prefix}", detailedStrings.prefixes["subsection"]!!)
            .replace("{label}", templates.subsectionLabels["total_score"] ?: "")
            .replace("{score}", info.totalScore.toString()))

        sb.appendLine(detailedStrings.infoFormats["grade"]!!
            .replace("{prefix}", detailedStrings.prefixes["subsection"]!!)
            .replace("{label}", templates.subsectionLabels["grade"] ?: "")
            .replace("{grade}", info.grade))

        sb.appendLine("\n${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["score_breakdown"]}:")

        val progressBar = detailedStrings.progressBar
        val barLength = (progressBar["length"] as? Number)?.toInt() ?: 20
        val barUnit = (progressBar["unit"] as? Number)?.toInt() ?: 5
        val filledBar = progressBar["filled"] as? String ?: "█"
        val emptyBar = progressBar["empty"] as? String ?: "□"

        info.scoreBreakdown.forEach { (category, score) ->
            val (current, max) = score
            val percentage = (current.toDouble() / max * detailedStrings.magicNumbers["max_score"]!!).toInt()
            val filledBars = percentage / barUnit
            val emptyBars = barLength - filledBars
            val bar = filledBar.repeat(filledBars) + emptyBar.repeat(emptyBars)
            sb.appendLine(progressBar["format"].toString()
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{category}", category)
                .replace("{current}", current.toString())
                .replace("{max}", max.toString())
                .replace("{bar}", bar)
                .replace("{percentage}", percentage.toString()))
        }
    }

    private fun appendSajuDetail(sb: StringBuilder, detail: SajuDetail) {
        val sectionNum = formatSettings.sectionNumbers["saju_analysis"] ?: "2"
        val sectionTitle = templates.sectionTitles["saju_analysis"] ?: ""

        sb.appendLine()
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))
        sb.appendLine("${detailedStrings.prefixes["section"]} $sectionNum. $sectionTitle")
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))

        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["saju_composition"]}:")
        detail.fourPillars.forEach { (pillar, interpretation) ->
            sb.appendLine(detailedStrings.sajuFormats["pillar"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{name}", pillar)
                .replace("{interpretation}", interpretation))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["element_distribution"]}:")

        val dotDisplay = detailedStrings.dotDisplay
        val maxDots = (dotDisplay["max"] as? Number)?.toInt() ?: 8
        val filledDot = dotDisplay["filled"] as? String ?: "●"
        val emptyDot = dotDisplay["empty"] as? String ?: "○"

        detail.elementDistribution.forEach { (element, count) ->
            val filledDots = minOf(count, maxDots)
            val emptyDots = maxDots - filledDots
            val bar = filledDot.repeat(filledDots) + emptyDot.repeat(emptyDots)
            sb.appendLine(dotDisplay["format"].toString()
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{element}", element)
                .replace("{bar}", bar)
                .replace("{count}", count.toString()))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["dominant_element"]}: ${detail.dominantElement}")
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["lacking_elements"]}: ${detail.lackingElements.joinToString(detailedStrings.listFormats["joined"]!!)}")
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["element_balance"]}: ${detail.elementBalance}")
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["seasonal_influence"]}: ${detail.seasonalInfluence}")
        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["day_master_analysis"]}:")
        sb.appendLine(detail.dayMasterAnalysis)
    }

    private fun appendStrokeDetail(sb: StringBuilder, detail: StrokeDetail) {
        val sectionNum = formatSettings.sectionNumbers["stroke_analysis"] ?: "3"
        val sectionTitle = templates.sectionTitles["stroke_analysis"] ?: ""

        sb.appendLine()
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))
        sb.appendLine("${detailedStrings.prefixes["section"]} $sectionNum. $sectionTitle")
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))

        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["stroke_numbers"]}:")
        detail.strokeNumbers.forEach { (name, stroke) ->
            sb.appendLine(detailedStrings.strokeFormats["number"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{name}", name)
                .replace("{stroke}", stroke.toString()))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["luck_analysis"]}:")
        detail.luckAnalysis.forEach { (name, analysis) ->
            sb.appendLine(detailedStrings.strokeFormats["luck"]!!
                .replace("{prefix}", detailedStrings.prefixes["subitem"]!!)
                .replace("{name}", name))
            analysis.lines().forEach { line ->
                sb.appendLine(detailedStrings.sajuFormats["analysis"]!!.replace("{line}", line))
            }
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["numerology_meaning"]}:")
        detail.numerologyMeaning.forEach { (name, meaning) ->
            sb.appendLine(detailedStrings.strokeFormats["meaning"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{name}", name)
                .replace("{meaning}", meaning))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["life_path_influence"]}:")
        sb.appendLine(detail.lifePathInfluence)
    }

    private fun appendElementDetail(sb: StringBuilder, detail: ElementDetail) {
        val sectionNum = formatSettings.sectionNumbers["element_analysis"] ?: "4"
        val sectionTitle = templates.sectionTitles["element_analysis"] ?: ""

        sb.appendLine()
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))
        sb.appendLine("${detailedStrings.prefixes["section"]} $sectionNum. $sectionTitle")
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))

        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["name_elements"]}: ${detail.nameElements.joinToString(detailedStrings.listFormats["joined"]!!)}")

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["element_relationships"]}:")
        detail.relationshipMap.forEach { relation ->
            sb.appendLine(detailedStrings.elementFormats["relation"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{from}", relation.from)
                .replace("{to}", relation.to)
                .replace("{type}", relation.type))
            sb.appendLine(detailedStrings.elementFormats["influence"]!!
                .replace("{prefix}", detailedStrings.prefixes["tree"]!!)
                .replace("{influence}", relation.influence))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["cycle_analysis"]}:")
        sb.appendLine(detail.cycleAnalysis)

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["strength_analysis"]}:")
        detail.strengthAnalysis.forEach { (position, strength) ->
            sb.appendLine(detailedStrings.elementFormats["strength"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{position}", position)
                .replace("{strength}", strength))
        }
    }

    private fun appendYinYangDetail(sb: StringBuilder, detail: YinYangDetail) {
        val sectionNum = formatSettings.sectionNumbers["yin_yang_analysis"] ?: "5"
        val sectionTitle = templates.sectionTitles["yin_yang_analysis"] ?: ""

        sb.appendLine()
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))
        sb.appendLine("${detailedStrings.prefixes["section"]} $sectionNum. $sectionTitle")
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))

        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["yin_yang_pattern"]}: ${detail.pattern}")
        sb.appendLine(detailedStrings.yinyangFormats["distribution"]!!
            .replace("{prefix}", detailedStrings.prefixes["subsection"]!!)
            .replace("{label}", templates.subsectionLabels["yin_yang_distribution"] ?: "")
            .replace("{yin}", (detail.distribution["음"] ?: 0).toString())
            .replace("{yang}", (detail.distribution["양"] ?: 0).toString()))
        sb.appendLine(detailedStrings.yinyangFormats["score"]!!
            .replace("{prefix}", detailedStrings.prefixes["subsection"]!!)
            .replace("{label}", templates.subsectionLabels["balance_score"] ?: "")
            .replace("{score}", detail.balanceScore.toString()))
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["energy_type"]}: ${detail.energyType}")
        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["energy_flow"]}:")
        sb.appendLine(detail.flowAnalysis)
    }

    private fun appendCharacterAnalysis(sb: StringBuilder, analysis: CharacterAnalysis) {
        val sectionNum = formatSettings.sectionNumbers["character_analysis"] ?: "6"
        val sectionTitle = templates.sectionTitles["character_analysis"] ?: ""

        sb.appendLine()
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))
        sb.appendLine("${detailedStrings.prefixes["section"]} $sectionNum. $sectionTitle")
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))

        analysis.hanjaDetails.forEach { hanja ->
            sb.appendLine(detailedStrings.characterFormats["hanja"]!!
                .replace("{prefix}", detailedStrings.prefixes["subsection"]!!)
                .replace("{character}", hanja.character)
                .replace("{meaning}", hanja.meaning))
            sb.appendLine(detailedStrings.characterFormats["origin"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{origin}", hanja.origin))
            sb.appendLine(detailedStrings.characterFormats["components"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{components}", hanja.components.joinToString(detailedStrings.listFormats["joined"]!!)))
            sb.appendLine(detailedStrings.characterFormats["related"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{related}", hanja.relatedCharacters.joinToString(detailedStrings.listFormats["joined"]!!)))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["combined_meaning"]}: ${analysis.combinedMeaning}")
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["symbolic_interpretation"]}: ${analysis.symbolicInterpretation}")
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["cultural_significance"]}: ${analysis.culturalSignificance}")
    }

    private fun appendFortunePrediction(sb: StringBuilder, fortune: FortunePrediction) {
        val sectionNum = formatSettings.sectionNumbers["fortune_prediction"] ?: "7"
        val sectionTitle = templates.sectionTitles["fortune_prediction"] ?: ""

        sb.appendLine()
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))
        sb.appendLine("${detailedStrings.prefixes["section"]} $sectionNum. $sectionTitle")
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))

        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["overall_fortune"]}: ${fortune.overallFortune}")

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["life_periods"]}:")
        fortune.lifePeriods.forEach { (period, fortuneDetail) ->
            sb.appendLine(detailedStrings.listFormats["item"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{item}", "$period: $fortuneDetail"))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["lucky_elements"]}: ${fortune.luckyElements.joinToString(detailedStrings.listFormats["joined"]!!)}")
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["challenge_periods"]}: ${fortune.challengePeriods.joinToString(detailedStrings.listFormats["joined"]!!)}")
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["opportunity_areas"]}: ${fortune.opportunityAreas.joinToString(detailedStrings.listFormats["joined"]!!)}")
    }

    private fun appendPersonalityAnalysis(sb: StringBuilder, personality: PersonalityAnalysis) {
        val sectionNum = formatSettings.sectionNumbers["personality_analysis"] ?: "8"
        val sectionTitle = templates.sectionTitles["personality_analysis"] ?: ""

        sb.appendLine()
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))
        sb.appendLine("${detailedStrings.prefixes["section"]} $sectionNum. $sectionTitle")
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))

        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["core_traits"]}: ${personality.coreTraits.joinToString(detailedStrings.listFormats["joined"]!!)}")

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["strengths"]}:")
        personality.strengths.forEach { strength ->
            sb.appendLine(detailedStrings.listFormats["item"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{item}", strength))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["weaknesses"]}:")
        personality.weaknesses.forEach { weakness ->
            sb.appendLine(detailedStrings.listFormats["item"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{item}", weakness))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["emotional_tendency"]}: ${personality.emotionalTendency}")
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["social_style"]}: ${personality.socialStyle}")
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["leadership_potential"]}: ${personality.leadershipPotential}")
    }

    private fun appendCareerGuidance(sb: StringBuilder, career: CareerGuidance) {
        val sectionNum = formatSettings.sectionNumbers["career_guidance"] ?: "9"
        val sectionTitle = templates.sectionTitles["career_guidance"] ?: ""

        sb.appendLine()
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))
        sb.appendLine("${detailedStrings.prefixes["section"]} $sectionNum. $sectionTitle")
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))

        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["suitable_fields"]}:")
        career.suitableFields.forEach { field ->
            sb.appendLine(detailedStrings.listFormats["item"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{item}", field))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["avoid_fields"]}:")
        career.avoidFields.forEach { field ->
            sb.appendLine(detailedStrings.listFormats["item"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{item}", field))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["work_style"]}: ${career.workStyle}")
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["success_factors"]}: ${career.successFactors.joinToString(detailedStrings.listFormats["joined"]!!)}")
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["business_luck"]}: ${career.businessLuck}")
    }

    private fun appendComprehensiveAdvice(sb: StringBuilder, advice: ComprehensiveAdvice) {
        val sectionNum = formatSettings.sectionNumbers["comprehensive_advice"] ?: "10"
        val sectionTitle = templates.sectionTitles["comprehensive_advice"] ?: ""

        sb.appendLine()
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))
        sb.appendLine("${detailedStrings.prefixes["section"]} $sectionNum. $sectionTitle")
        sb.appendLine(detailedStrings.separators["section"]?.repeat(detailedStrings.sectionSeparatorLength))

        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["summary"]}:")
        sb.appendLine(advice.summary)

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["key_recommendations"]}:")
        advice.keyRecommendations.forEach { recommendation ->
            sb.appendLine(detailedStrings.listFormats["item"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{item}", recommendation))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["caution_points"]}:")
        advice.cautionPoints.forEach { point ->
            sb.appendLine(detailedStrings.listFormats["item"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{item}", point))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["development_areas"]}:")
        advice.developmentAreas.forEach { area ->
            sb.appendLine(detailedStrings.listFormats["item"]!!
                .replace("{prefix}", detailedStrings.prefixes["item"]!!)
                .replace("{item}", area))
        }

        sb.appendLine()
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["lucky_colors"]}: ${advice.luckyColors.joinToString(detailedStrings.listFormats["joined"]!!)}")
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["lucky_numbers"]}: ${advice.luckyNumbers.joinToString(detailedStrings.listFormats["joined"]!!)}")
        sb.appendLine("${detailedStrings.prefixes["subsection"]} ${templates.subsectionLabels["compatible_names"]}: ${advice.compatibleNames.joinToString(detailedStrings.listFormats["joined"]!!)}")
    }
}