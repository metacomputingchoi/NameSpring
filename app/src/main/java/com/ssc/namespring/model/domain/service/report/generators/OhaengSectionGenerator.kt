// model/domain/service/report/generators/OhaengSectionGenerator.kt
package com.ssc.namespring.model.domain.service.report.generators

import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.OhaengInfo
import com.ssc.namespring.model.domain.service.report.interfaces.IReportSectionGenerator

class OhaengSectionGenerator : IReportSectionGenerator {

    override fun getSectionName(): String = "ohaengAnalysis"

    override fun generateSection(profile: Profile): Map<String, Any> {
        val ohaeng = profile.ohaengInfo ?: return emptyMap()
        val total = calculateTotal(ohaeng)

        return mapOf(
            "counts" to createCountsMap(ohaeng),
            "percentages" to createPercentagesMap(ohaeng, total),
            "total" to total,
            "balance" to calculateBalance(ohaeng)
        )
    }

    private fun calculateTotal(ohaeng: OhaengInfo): Int {
        return ohaeng.wood + ohaeng.fire + ohaeng.earth + ohaeng.metal + ohaeng.water
    }

    private fun createCountsMap(ohaeng: OhaengInfo): Map<String, Int> {
        return mapOf(
            "wood" to ohaeng.wood,
            "fire" to ohaeng.fire,
            "earth" to ohaeng.earth,
            "metal" to ohaeng.metal,
            "water" to ohaeng.water
        )
    }

    private fun createPercentagesMap(ohaeng: OhaengInfo, total: Int): Map<String, String> {
        if (total <= 0) return emptyMap()

        return mapOf(
            "wood" to formatPercentage(ohaeng.wood, total),
            "fire" to formatPercentage(ohaeng.fire, total),
            "earth" to formatPercentage(ohaeng.earth, total),
            "metal" to formatPercentage(ohaeng.metal, total),
            "water" to formatPercentage(ohaeng.water, total)
        )
    }

    private fun formatPercentage(value: Int, total: Int): String {
        return String.format("%.1f%%", (value * 100.0 / total))
    }

    private fun calculateBalance(ohaeng: OhaengInfo): String {
        val counts = listOf(ohaeng.wood, ohaeng.fire, ohaeng.earth, ohaeng.metal, ohaeng.water)
        val max = counts.maxOrNull() ?: 0
        val min = counts.minOrNull() ?: 0
        val diff = max - min

        return when {
            diff <= 2 -> "매우 균형잡힌 오행"
            diff <= 4 -> "균형잡힌 오행"
            diff <= 6 -> "약간 불균형한 오행"
            else -> "불균형한 오행"
        }
    }
}