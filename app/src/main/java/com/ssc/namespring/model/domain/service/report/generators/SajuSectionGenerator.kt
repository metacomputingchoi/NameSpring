// model/domain/service/report/generators/SajuSectionGenerator.kt
package com.ssc.namespring.model.domain.service.report.generators

import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.Pillar
import com.ssc.namespring.model.domain.service.report.interfaces.IReportSectionGenerator

class SajuSectionGenerator : IReportSectionGenerator {

    override fun getSectionName(): String = "sajuAnalysis"

    override fun generateSection(profile: Profile): Map<String, Any> {
        val saju = profile.sajuInfo ?: return emptyMap()

        return mapOf(
            "fourPillars" to saju.fourPillars,
            "yearPillar" to createPillarMap(saju.yearPillar),
            "monthPillar" to createPillarMap(saju.monthPillar),
            "dayPillar" to createPillarMap(saju.dayPillar),
            "hourPillar" to createPillarMap(saju.hourPillar),
            "elementCounts" to saju.sajuOhaengCount,
            "missingElements" to saju.missingElements,
            "dominantElements" to saju.dominantElements,
            "elementBalance" to saju.elementBalance
        )
    }

    private fun createPillarMap(pillar: Pillar): Map<String, String> {
        return mapOf(
            "heavenlyStem" to pillar.heavenlyStem,
            "earthlyBranch" to pillar.earthlyBranch,
            "ohaeng" to "${pillar.stemOhaeng}${pillar.branchOhaeng}"
        )
    }
}