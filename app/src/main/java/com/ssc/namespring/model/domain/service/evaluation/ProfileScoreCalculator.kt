// model/domain/service/evaluation/ProfileScoreCalculator.kt
package com.ssc.namespring.model.domain.service.evaluation

import android.util.Log
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.domain.service.evaluation.calculators.*
import com.ssc.namespring.model.domain.service.evaluation.config.ScoreWeightConfig
import kotlin.math.roundToInt

object ProfileScoreCalculator {
    private const val TAG = "ProfileScoreCalculator"
    private val weightConfig = ScoreWeightConfig()

    private val calculators = mapOf(
        "음양균형" to EumyangScoreCalculator(),
        "오행조화" to OhaengScoreCalculator(),
        "한자의미" to MeaningScoreCalculator(),
        "발음자연스러움" to PronunciationScoreCalculator(),
        "사주보완" to SajuComplementScoreCalculator()
    )

    fun calculateNamebomScore(generatedName: GeneratedName): Int {
        val analysisInfo = generatedName.analysisInfo ?: return 0

        Log.d(TAG, "=== 이름봄 점수 계산 시작 ===")

        val scores = calculateAllScores(generatedName, analysisInfo)
        val finalScore = calculateWeightedScore(scores)

        logScoreDetails(scores, finalScore)

        return finalScore
    }

    private fun calculateAllScores(
        generatedName: GeneratedName,
        analysisInfo: com.ssc.namingengine.data.analysis.NameAnalysisInfo
    ): Map<String, Int> {
        val scores = mutableMapOf<String, Int>()

        // 사격수리 점수
        val sagyeokResult = SagyeokScoreCalculator.calculateSagyeokScores(generatedName)
        scores["사격수리"] = sagyeokResult.weightedTotal + sagyeokResult.harmonyBonus

        // 나머지 점수들
        scores["음양균형"] = calculators["음양균형"]!!.calculate(analysisInfo.eumYangInfo)
        scores["오행조화"] = calculators["오행조화"]!!.calculate(
            OhaengScoreCalculator.OhaengCalculationData(analysisInfo.ohaengInfo, analysisInfo.sajuInfo)
        )
        scores["한자의미"] = calculators["한자의미"]!!.calculate(generatedName)
        scores["발음자연스러움"] = calculators["발음자연스러움"]!!.calculate(analysisInfo.filteringSteps)
        scores["사주보완"] = calculators["사주보완"]!!.calculate(
            SajuComplementScoreCalculator.SajuCalculationData(
                analysisInfo.ohaengInfo.jawonOhaeng,
                analysisInfo.sajuInfo
            )
        )

        return scores
    }

    private fun calculateWeightedScore(scores: Map<String, Int>): Int {
        var weightedSum = 0f

        scores.forEach { (category, score) ->
            weightedSum += score * weightConfig.getWeight(category)
        }

        return weightedSum.roundToInt().coerceIn(0, 100)
    }

    private fun logScoreDetails(scores: Map<String, Int>, finalScore: Int) {
        val details = scores.map { (category, score) ->
            val weight = weightConfig.getWeight(category)
            val weightedScore = (score * weight).roundToInt()
            "$category (${(weight * 100).toInt()}%): ${score}점 → ${weightedScore}점"
        }.joinToString("\n")

        Log.d(TAG, """
            === 점수 내역 ===
            $details
            === 최종 점수: ${finalScore}점 ===
        """.trimIndent())
    }
}
