// model/domain/service/factory/SajuFactory.kt
package com.ssc.namespring.model.domain.service.factory

import com.ssc.namespring.model.domain.entity.Pillar
import com.ssc.namespring.model.domain.entity.SajuInfo
import com.ssc.namingengine.data.analysis.NameAnalysisInfo

object SajuFactory {
    fun createFromAnalysisInfo(analysisInfo: NameAnalysisInfo): SajuInfo {
        val sajuAnalysisInfo = analysisInfo.sajuInfo
        val fourPillarsList = sajuAnalysisInfo.fourPillars.toList()

        return SajuInfo(
            fourPillars = fourPillarsList,
            yearPillar = Pillar.fromPillarString(fourPillarsList[0]),
            monthPillar = Pillar.fromPillarString(fourPillarsList[1]),
            dayPillar = Pillar.fromPillarString(fourPillarsList[2]),
            hourPillar = Pillar.fromPillarString(fourPillarsList[3]),
            sajuOhaengCount = sajuAnalysisInfo.sajuOhaengCount,
            missingElements = sajuAnalysisInfo.missingElements,
            dominantElements = sajuAnalysisInfo.dominantElements,
            elementBalance = sajuAnalysisInfo.elementBalance
        )
    }
}
