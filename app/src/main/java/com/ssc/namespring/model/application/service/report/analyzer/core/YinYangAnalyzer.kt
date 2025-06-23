// model/application/service/report/analyzer/core/YinYangAnalyzer.kt
package com.ssc.namespring.model.application.service.report.analyzer.core

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.YinYangDetail
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class YinYangAnalyzer {
    private val yinYangData = ReportDataHolder.yinYangPatternsData
    private val strings = ReportDataHolder.yinYangAnalyzerStrings

    fun analyzeBalance(result: Name): String {
        val pmPattern = result.combinedPm ?: return strings.errorMessage

        val (yinCount, yangCount) = countYinYang(pmPattern)
        val balanceAnalysis = analyzeBalancePattern(pmPattern)

        return strings.balanceTemplate
            .replace("{pattern}", pmPattern)
            .replace("{yin}", yinCount.toString())
            .replace("{yang}", yangCount.toString())
            .replace("{analysis}", balanceAnalysis)
    }

    fun analyzeDetailed(result: Name): YinYangDetail {
        val pmPattern = result.combinedPm ?: strings.defaultPattern
        val (yinCount, yangCount) = countYinYang(pmPattern)

        return YinYangDetail(
            pattern = pmPattern,
            distribution = mapOf("음" to yinCount, "양" to yangCount),
            balanceScore = calculateBalanceScore(yinCount, yangCount),
            flowAnalysis = analyzeEnergyFlow(pmPattern),
            energyType = determineEnergyType(yinCount, yangCount)
        )
    }

    private fun countYinYang(pmPattern: String): Pair<Int, Int> {
        val yinChar = strings.yinChar.firstOrNull() ?: '0'
        val yangChar = strings.yangChar.firstOrNull() ?: '1'

        val yinCount = pmPattern.count { it == yinChar }
        val yangCount = pmPattern.count { it == yangChar }
        return yinCount to yangCount
    }

    private fun analyzeBalancePattern(pmPattern: String): String {
        val setSize = strings.magicNumbers["yin_yang_set_size"]!!

        return if (pmPattern.toSet().size == setSize) {
            strings.balancePatterns["balanced"]!!
        } else {
            strings.balancePatterns["unbalanced"]!!
        }
    }

    private fun calculateBalanceScore(yinCount: Int, yangCount: Int): Int {
        return when {
            yinCount == yangCount -> strings.balanceScores["perfect"]!!
            kotlin.math.abs(yinCount - yangCount) == strings.balanceThresholds["good"]!! -> strings.balanceScores["good"]!!
            kotlin.math.abs(yinCount - yangCount) == strings.balanceThresholds["fair"]!! -> strings.balanceScores["fair"]!!
            else -> strings.balanceScores["poor"]!!
        }
    }

    private fun analyzeEnergyFlow(pmPattern: String): String {
        val transitions = mutableListOf<String>()
        val transitionDescriptions = yinYangData.yinYangTransitions

        for (i in 0 until pmPattern.length - 1) {
            val transition = "${pmPattern[i]}${pmPattern[i + 1]}"
            transitionDescriptions[transition]?.let { transitions.add(it) }
        }

        val patternAnalysis = yinYangData.patternAnalyses[pmPattern] ?: strings.uniquePatternMessage

        return transitions.joinToString("\n") + "\n" + patternAnalysis
    }

    private fun determineEnergyType(yinCount: Int, yangCount: Int): String {
        val energyTypes = yinYangData.energyTypes
        return when {
            yinCount > yangCount -> energyTypes["yin_dominant"] ?: ""
            yangCount > yinCount -> energyTypes["yang_dominant"] ?: ""
            else -> energyTypes["balanced"] ?: ""
        }
    }
}