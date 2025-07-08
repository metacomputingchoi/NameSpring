// model/domain/service/evaluation/calculators/MeaningScoreCalculator.kt
package com.ssc.namespring.model.domain.service.evaluation.calculators

import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.domain.service.evaluation.interfaces.IScoreCalculator
import com.ssc.namespring.utils.data.json.JsonLoader

class MeaningScoreCalculator : IScoreCalculator {
    override fun calculate(data: Any): Int {
        val generatedName = data as? GeneratedName ?: return 0
        var score = 70

        // 각 한자의 의미 평가
        score += evaluateIndividualMeanings(generatedName)

        // 한자 간 의미 조화 체크
        score += evaluateMeaningHarmony(generatedName)

        return score.coerceIn(0, 100)
    }

    private fun evaluateIndividualMeanings(generatedName: GeneratedName): Int {
        var additionalScore = 0

        generatedName.hanjaDetails.forEach { hanjaInfo ->
            val meaning = hanjaInfo.inmyongMeaning ?: return@forEach

            if (JsonLoader.hasPositiveMeaning(meaning)) {
                additionalScore += 10
            }

            JsonLoader.getHanjaMeaning(hanjaInfo.hanja)?.let { hanjaMeaning ->
                if (hanjaMeaning.origin != null) additionalScore += 5
                if (!hanjaMeaning.relatedCharacters.isNullOrEmpty()) additionalScore += 5
            }
        }

        return additionalScore
    }

    private fun evaluateMeaningHarmony(generatedName: GeneratedName): Int {
        if (generatedName.hanjaDetails.size < 2) return 0

        val meaning1 = generatedName.hanjaDetails[0].inmyongMeaning ?: ""
        val meaning2 = generatedName.hanjaDetails[1].inmyongMeaning ?: ""

        return if (JsonLoader.isMeaningHarmony(meaning1, meaning2)) 10 else 0
    }

    override fun getName(): String = "한자의미"
}
