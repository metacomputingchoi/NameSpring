// model/application/service/report/analyzer/core/SajuAnalyzer.kt
package com.ssc.namespring.model.application.service.report.analyzer.core

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.SajuDetail
import com.ssc.namespring.model.common.constants.Constants
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class SajuAnalyzer {
    private val pillarData = ReportDataHolder.pillarInterpretationsData
    private val strings = ReportDataHolder.sajuAnalyzerStrings

    fun analyzeBasic(result: Name): String {
        val saju = result.sajuInfo
        val elementCount = result.dictElementsCount.toMap()

        val baseAnalysis = buildBaseAnalysis(saju, elementCount)
        val supplementAnalysis = buildSupplementAnalysis(result.zeroElements, result.oneElements)

        return if (supplementAnalysis.isNotEmpty()) {
            "$baseAnalysis$supplementAnalysis"
        } else {
            baseAnalysis
        }
    }

    fun analyzeDetailed(result: Name): SajuDetail {
        val saju = result.sajuInfo
        val elementCount = result.dictElementsCount.toMap()

        return SajuDetail(
            fourPillars = analyzeFourPillars(saju),
            elementDistribution = elementCount,
            dominantElement = findDominantElement(elementCount),
            lackingElements = findLackingElements(elementCount),
            elementBalance = analyzeElementBalance(elementCount),
            seasonalInfluence = analyzeSeasonalInfluence(saju.wolju),
            dayMasterAnalysis = analyzeDayMaster(saju.ilju, elementCount)
        )
    }

    private fun buildBaseAnalysis(saju: com.ssc.namespring.model.domain.saju.entity.Saju, elementCount: Map<String, Int>): String {
        return strings.baseAnalysisTemplate
            .replace("{yeonju}", saju.yeonju)
            .replace("{wolju}", saju.wolju)
            .replace("{ilju}", saju.ilju)
            .replace("{siju}", saju.siju)
            .replace("{wood}", (elementCount["木"] ?: 0).toString())
            .replace("{fire}", (elementCount["火"] ?: 0).toString())
            .replace("{earth}", (elementCount["土"] ?: 0).toString())
            .replace("{metal}", (elementCount["金"] ?: 0).toString())
            .replace("{water}", (elementCount["水"] ?: 0).toString())
    }

    private fun buildSupplementAnalysis(zeroElements: List<String>, oneElements: List<String>): String {
        return when {
            zeroElements.isNotEmpty() -> strings.supplementAnalysis["zero_elements"]!!
                .replace("{elements}", zeroElements.joinToString(", "))
            oneElements.isNotEmpty() -> strings.supplementAnalysis["one_elements"]!!
                .replace("{elements}", oneElements.joinToString(", "))
            else -> ""
        }
    }

    private fun analyzeFourPillars(saju: com.ssc.namespring.model.domain.saju.entity.Saju): Map<String, String> {
        val interpretations = pillarData.pillarInterpretations
        return mapOf(
            strings.pillarNames["year"]!! to strings.pillarFormat
                .replace("{name}", saju.yeonju)
                .replace("{interpretation}", interpretations["연주"] ?: ""),
            strings.pillarNames["month"]!! to strings.pillarFormat
                .replace("{name}", saju.wolju)
                .replace("{interpretation}", interpretations["월주"] ?: ""),
            strings.pillarNames["day"]!! to strings.pillarFormat
                .replace("{name}", saju.ilju)
                .replace("{interpretation}", interpretations["일주"] ?: ""),
            strings.pillarNames["hour"]!! to strings.pillarFormat
                .replace("{name}", saju.siju)
                .replace("{interpretation}", interpretations["시주"] ?: "")
        )
    }

    private fun findDominantElement(elementCount: Map<String, Int>): String {
        val maxElement = elementCount.maxByOrNull { it.value }
        return strings.dominantElementFormat
            .replace("{element}", maxElement?.key ?: strings.defaultValues["none"]!!)
            .replace("{count}", (maxElement?.value ?: 0).toString())
    }

    private fun findLackingElements(elementCount: Map<String, Int>): List<String> {
        return elementCount.filter { it.value <= 1 }.keys.toList()
    }

    private fun analyzeElementBalance(elementCount: Map<String, Int>): String {
        val variance = elementCount.values.let { values ->
            val avg = values.average()
            values.map { (it - avg) * (it - avg) }.average()
        }

        val descriptions = pillarData.elementBalanceDescriptions
        return when {
            variance < strings.magicNumbers["variance_very_balanced"]!! -> descriptions["very_balanced"] ?: ""
            variance < strings.magicNumbers["variance_balanced"]!! -> descriptions["balanced"] ?: ""
            variance < strings.magicNumbers["variance_slightly_unbalanced"]!! -> descriptions["slightly_unbalanced"] ?: ""
            else -> descriptions["very_unbalanced"] ?: ""
        }
    }

    private fun analyzeSeasonalInfluence(wolju: String): String {
        val branch = wolju[1]
        val seasonalInfluences = pillarData.seasonalInfluences

        for ((season, data) in seasonalInfluences) {
            if (branch.toString() in data.branches) {
                return data.influence
            }
        }

        return strings.defaultValues["season_error"]!!
    }

    private fun analyzeDayMaster(ilju: String, elementCount: Map<String, Int>): String {
        val dayStem = ilju[0]
        val dayElement = Constants.STEM_ELEMENTS[dayStem.toString()] ?: return strings.defaultValues["analysis_error"]!!
        val dayElementCount = elementCount[dayElement] ?: 0

        val evaluations = pillarData.dayMasterEvaluations
        val evaluation = when (dayElementCount) {
            0, 1 -> evaluations["0_1"]
            strings.magicNumbers["day_master_normal"]!!.toInt() -> evaluations["2"]
            strings.magicNumbers["day_master_strong"]!!.toInt() -> evaluations["3"]
            strings.magicNumbers["day_master_very_strong_min"]!!.toInt(),
            strings.magicNumbers["day_master_very_strong_max"]!!.toInt() -> evaluations["4_5"]
            else -> evaluations["6_above"]
        } ?: strings.defaultValues["analysis_error"]!!

        return strings.dayMasterTemplate
            .replace("{stem}", dayStem.toString())
            .replace("{element}", dayElement)
            .replace("{count}", dayElementCount.toString())
            .replace("{evaluation}", evaluation)
    }
}