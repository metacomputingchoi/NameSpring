// model/domain/service/evaluation/ProfileScoreCalculator.kt
package com.ssc.namespring.model.domain.service.evaluation

import android.util.Log
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.utils.data.json.JsonLoader
import com.ssc.namingengine.data.analysis.component.EumYangAnalysisInfo
import kotlin.math.roundToInt

object ProfileScoreCalculator {
    private const val TAG = "ProfileScoreCalculator"

    // 평가 항목별 가중치 (총 100%)
    private val SCORE_WEIGHTS = mapOf(
        "사격수리" to 0.35f,      // 35% - 가장 중요
        "음양균형" to 0.15f,      // 15%
        "오행조화" to 0.20f,      // 20%
        "한자의미" to 0.15f,      // 15%
        "발음자연스러움" to 0.10f, // 10%
        "사주보완" to 0.05f       // 5%
    )

    fun calculateNamebomScore(generatedName: GeneratedName): Int {
        val analysisInfo = generatedName.analysisInfo ?: return 0

        Log.d(TAG, "=== 이름봄 점수 계산 시작 ===")

        // 1. 사격수리 점수 (35%)
        val sagyeokResult = SagyeokScoreCalculator.calculateSagyeokScores(generatedName)
        val sagyeokScore = sagyeokResult.weightedTotal + sagyeokResult.harmonyBonus

        // 2. 음양균형 점수 (15%)
        val eumyangScore = calculateEumyangScore(analysisInfo.eumYangInfo)

        // 3. 오행조화 점수 (20%)
        val ohaengScore = calculateOhaengScore(analysisInfo.ohaengInfo, analysisInfo.sajuInfo)

        // 4. 한자의미 점수 (15%)
        val meaningScore = calculateMeaningScore(generatedName)

        // 5. 발음 자연스러움 점수 (10%)
        val pronunciationScore = calculatePronunciationScore(analysisInfo.filteringSteps)

        // 6. 사주보완 점수 (5%)
        val sajuComplementScore = calculateSajuComplementScore(
            analysisInfo.ohaengInfo.jawonOhaeng,
            analysisInfo.sajuInfo
        )

        Log.d(TAG, """
            === 점수 계산 상세 ===
            analysisInfo 존재: ${analysisInfo != null}
            sagyeok 정보: ${generatedName.sagyeok}
            eumYangInfo: ${analysisInfo.eumYangInfo}
            ohaengInfo: ${analysisInfo.ohaengInfo}
        """.trimIndent())

        // 가중치 적용한 최종 점수 계산
        val finalScore = (
                sagyeokScore * SCORE_WEIGHTS["사격수리"]!! +
                        eumyangScore * SCORE_WEIGHTS["음양균형"]!! +
                        ohaengScore * SCORE_WEIGHTS["오행조화"]!! +
                        meaningScore * SCORE_WEIGHTS["한자의미"]!! +
                        pronunciationScore * SCORE_WEIGHTS["발음자연스러움"]!! +
                        sajuComplementScore * SCORE_WEIGHTS["사주보완"]!!
                ).roundToInt().coerceIn(0, 100)

        Log.d(TAG, """
            === 점수 내역 ===
            사격수리 (35%): ${sagyeokScore}점 → ${(sagyeokScore * 0.35f).roundToInt()}점
            음양균형 (15%): ${eumyangScore}점 → ${(eumyangScore * 0.15f).roundToInt()}점
            오행조화 (20%): ${ohaengScore}점 → ${(ohaengScore * 0.20f).roundToInt()}점
            한자의미 (15%): ${meaningScore}점 → ${(meaningScore * 0.15f).roundToInt()}점
            발음자연 (10%): ${pronunciationScore}점 → ${(pronunciationScore * 0.10f).roundToInt()}점
            사주보완 (5%): ${sajuComplementScore}점 → ${(sajuComplementScore * 0.05f).roundToInt()}점
            === 최종 점수: ${finalScore}점 ===
        """.trimIndent())

        return finalScore
    }

    private fun calculateEumyangScore(eumYangInfo: EumYangAnalysisInfo): Int {
        return when {
            eumYangInfo.isBalanced -> {
                when (eumYangInfo.balance) {
                    in 0.4f..0.6f -> 100  // 완벽한 균형
                    in 0.3f..0.7f -> 85   // 좋은 균형
                    else -> 70            // 보통 균형
                }
            }
            eumYangInfo.pattern.contains("단일") -> 30  // 모두 음 또는 모두 양
            else -> 50  // 불균형
        }
    }

    private fun calculateOhaengScore(
        ohaengInfo: com.ssc.namingengine.data.analysis.component.OhaengAnalysisInfo,
        sajuInfo: com.ssc.namingengine.data.analysis.component.SajuAnalysisInfo
    ): Int {
        var score = 60  // 기본 점수

        // 발음오행 조화 체크
        val baleumOhaeng = ohaengInfo.baleumOhaeng
        if (baleumOhaeng.all { it == baleumOhaeng[0] }) {
            score -= 20  // 모두 같은 오행이면 감점
        }

        // 상생 관계 보너스
        score += ohaengInfo.generatingPairs.size * 10

        // 상극 관계 감점
        score -= ohaengInfo.conflictingPairs.size * 15

        // 오행 균형도에 따른 조정
        if (ohaengInfo.overallHarmony.contains("조화")) {
            score += 20
        }

        return score.coerceIn(0, 100)
    }

    private fun calculateMeaningScore(generatedName: GeneratedName): Int {
        var score = 70  // 기본 점수

        // 각 한자의 의미 평가
        generatedName.hanjaDetails.forEach { hanjaInfo ->
            val meaning = hanjaInfo.inmyongMeaning ?: return@forEach

            // 긍정적 의미 체크
            if (JsonLoader.hasPositiveMeaning(meaning)) {
                score += 10
            }

            // 한자 의미 정보 추가 분석
            JsonLoader.getHanjaMeaning(hanjaInfo.hanja)?.let { hanjaMeaning ->
                if (hanjaMeaning.origin != null) score += 5
                if (hanjaMeaning.relatedCharacters?.isNotEmpty() == true) score += 5
            }
        }

        // 한자 간 의미 조화 체크
        if (generatedName.hanjaDetails.size >= 2) {
            val meaning1 = generatedName.hanjaDetails[0].inmyongMeaning ?: ""
            val meaning2 = generatedName.hanjaDetails[1].inmyongMeaning ?: ""
            if (JsonLoader.isMeaningHarmony(meaning1, meaning2)) {
                score += 10
            }
        }

        return score.coerceIn(0, 100)
    }

    private fun calculatePronunciationScore(
        filteringSteps: List<com.ssc.namingengine.data.analysis.FilteringStep>
    ): Int {
        val pronunciationFilter = filteringSteps.find {
            it.filterName.contains("발음자연스러움")
        }

        return when {
            pronunciationFilter?.passed == true -> 90
            pronunciationFilter?.reason?.contains("사전 등재") == true -> 80
            else -> 60
        }
    }

    private fun calculateSajuComplementScore(
        jawonOhaeng: List<String>,
        sajuInfo: com.ssc.namingengine.data.analysis.component.SajuAnalysisInfo
    ): Int {
        var score = 50  // 기본 점수

        jawonOhaeng.forEach { element ->
            when {
                sajuInfo.missingElements.contains(element) -> score += 25  // 부족한 오행 보완
                sajuInfo.dominantElements.contains(element) -> score -= 15  // 과다한 오행 추가
                (sajuInfo.sajuOhaengCount[element] ?: 0) <= 1 -> score += 10  // 적은 오행 보완
            }
        }

        return score.coerceIn(0, 100)
    }
}