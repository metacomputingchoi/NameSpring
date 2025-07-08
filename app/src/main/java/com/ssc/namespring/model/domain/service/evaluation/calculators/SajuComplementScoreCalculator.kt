// model/domain/service/evaluation/calculators/SajuComplementScoreCalculator.kt
package com.ssc.namespring.model.domain.service.evaluation.calculators

import com.ssc.namingengine.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.domain.service.evaluation.interfaces.IScoreCalculator

class SajuComplementScoreCalculator : IScoreCalculator {
    data class SajuCalculationData(
        val jawonOhaeng: List<String>,
        val sajuInfo: SajuAnalysisInfo
    )

    override fun calculate(data: Any): Int {
        val calcData = data as? SajuCalculationData ?: return 0
        var score = 50

        calcData.jawonOhaeng.forEach { element ->
            score += calculateElementScore(element, calcData.sajuInfo)
        }

        return score.coerceIn(0, 100)
    }

    private fun calculateElementScore(element: String, sajuInfo: SajuAnalysisInfo): Int {
        return when {
            sajuInfo.missingElements.contains(element) -> 25
            sajuInfo.dominantElements.contains(element) -> -15
            (sajuInfo.sajuOhaengCount[element] ?: 0) <= 1 -> 10
            else -> 0
        }
    }

    override fun getName(): String = "사주보완"
}
