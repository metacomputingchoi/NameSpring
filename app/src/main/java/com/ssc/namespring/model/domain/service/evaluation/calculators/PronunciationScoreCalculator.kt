// model/domain/service/evaluation/calculators/PronunciationScoreCalculator.kt
package com.ssc.namespring.model.domain.service.evaluation.calculators

import com.ssc.namingengine.data.analysis.FilteringStep
import com.ssc.namespring.model.domain.service.evaluation.interfaces.IScoreCalculator

class PronunciationScoreCalculator : IScoreCalculator {
    override fun calculate(data: Any): Int {
        @Suppress("UNCHECKED_CAST")
        val filteringSteps = data as? List<FilteringStep> ?: return 60

        val pronunciationFilter = filteringSteps.find {
            it.filterName.contains("발음자연스러움")
        }

        return when {
            pronunciationFilter?.passed == true -> 90
            pronunciationFilter?.reason?.contains("사전 등재") == true -> 80
            else -> 60
        }
    }

    override fun getName(): String = "발음자연스러움"
}
