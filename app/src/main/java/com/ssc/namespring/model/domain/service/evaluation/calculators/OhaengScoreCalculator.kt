// model/domain/service/evaluation/calculators/OhaengScoreCalculator.kt
package com.ssc.namespring.model.domain.service.evaluation.calculators

import com.ssc.namingengine.data.analysis.component.OhaengAnalysisInfo
import com.ssc.namingengine.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.domain.service.evaluation.interfaces.IScoreCalculator

class OhaengScoreCalculator : IScoreCalculator {
    data class OhaengCalculationData(
        val ohaengInfo: OhaengAnalysisInfo,
        val sajuInfo: SajuAnalysisInfo
    )

    override fun calculate(data: Any): Int {
        val calcData = data as? OhaengCalculationData ?: return 0
        var score = 60

        // 발음오행 조화 체크
        val baleumOhaeng = calcData.ohaengInfo.baleumOhaeng
        if (baleumOhaeng.all { it == baleumOhaeng[0] }) {
            score -= 20
        }

        // 상생/상극 관계 계산
        score += calcData.ohaengInfo.generatingPairs.size * 10
        score -= calcData.ohaengInfo.conflictingPairs.size * 15

        // 오행 균형도 보너스
        if (calcData.ohaengInfo.overallHarmony.contains("조화")) {
            score += 20
        }

        return score.coerceIn(0, 100)
    }

    private fun isAllSameOhaeng(baleumOhaeng: List<String>): Boolean {
        return baleumOhaeng.all { it == baleumOhaeng[0] }
    }

    override fun getName(): String = "오행조화"
}
