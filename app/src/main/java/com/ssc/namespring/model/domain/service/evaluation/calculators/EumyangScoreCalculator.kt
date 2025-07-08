// model/domain/service/evaluation/calculators/EumyangScoreCalculator.kt
package com.ssc.namespring.model.domain.service.evaluation.calculators

import com.ssc.namingengine.data.analysis.component.EumYangAnalysisInfo
import com.ssc.namespring.model.domain.service.evaluation.interfaces.IScoreCalculator

class EumyangScoreCalculator : IScoreCalculator {
    override fun calculate(data: Any): Int {
        val eumYangInfo = data as? EumYangAnalysisInfo ?: return 0

        return when {
            eumYangInfo.isBalanced -> calculateBalancedScore(eumYangInfo.balance)
            eumYangInfo.pattern.contains("단일") -> 30
            else -> 50
        }
    }

    private fun calculateBalancedScore(balance: Float): Int {
        return when (balance) {
            in 0.4f..0.6f -> 100
            in 0.3f..0.7f -> 85
            else -> 70
        }
    }

    override fun getName(): String = "음양균형"
}
