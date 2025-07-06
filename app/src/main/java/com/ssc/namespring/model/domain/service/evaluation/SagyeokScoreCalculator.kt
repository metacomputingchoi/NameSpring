// model/domain/service/evaluation/SagyeokScoreCalculator.kt
package com.ssc.namespring.model.domain.service.evaluation

import android.util.Log
import com.ssc.namespring.utils.data.json.JsonLoader
import com.ssc.namespring.model.data.source.SimpleStrokeMeaning
import com.ssc.namingengine.data.GeneratedName

object SagyeokScoreCalculator {
    private const val TAG = "SagyeokScoreCalculator"

    // lucky_level에 따른 기본 점수
    private val LUCKY_LEVEL_SCORES = mapOf(
        "최상운수" to 100,
        "상운수" to 80,
        "양운수" to 60,
        "흉운수" to 30,
        "최흉운수" to 10
    )

    // 사격별 가중치 (정통 성명학 기준)
    private val SAGYEOK_WEIGHTS = mapOf(
        "형격" to 0.3f,  // 인격 - 가장 중요
        "원격" to 0.25f, // 총격
        "이격" to 0.25f, // 지격
        "정격" to 0.2f   // 외격
    )

    fun calculateSagyeokScores(generatedName: GeneratedName): SagyeokScoreResult {
        val sagyeok = generatedName.sagyeok

        // 각 사격의 획수 정규화 (81수리 기준)
        val hyeongNormalized = normalizeStroke(sagyeok.hyeong)
        val wonNormalized = normalizeStroke(sagyeok.won)
        val iNormalized = normalizeStroke(sagyeok.i)
        val jeongNormalized = normalizeStroke(sagyeok.jeong)

        // 각 사격의 길흉 정보 조회
        val hyeongMeaning = JsonLoader.getStrokeMeaning(hyeongNormalized)
        val wonMeaning = JsonLoader.getStrokeMeaning(wonNormalized)
        val iMeaning = JsonLoader.getStrokeMeaning(iNormalized)
        val jeongMeaning = JsonLoader.getStrokeMeaning(jeongNormalized)

        // 각 사격 점수 계산
        val hyeongScore = LUCKY_LEVEL_SCORES[hyeongMeaning.luck] ?: 50
        val wonScore = LUCKY_LEVEL_SCORES[wonMeaning.luck] ?: 50
        val iScore = LUCKY_LEVEL_SCORES[iMeaning.luck] ?: 50
        val jeongScore = LUCKY_LEVEL_SCORES[jeongMeaning.luck] ?: 50

        // 가중치 적용한 총점
        val weightedTotal = (hyeongScore * SAGYEOK_WEIGHTS["형격"]!! +
                wonScore * SAGYEOK_WEIGHTS["원격"]!! +
                iScore * SAGYEOK_WEIGHTS["이격"]!! +
                jeongScore * SAGYEOK_WEIGHTS["정격"]!!).toInt()

        // 사격 간 조화 점수 (추가 보너스)
        val harmonyBonus = calculateSagyeokHarmony(
            listOf(hyeongMeaning, wonMeaning, iMeaning, jeongMeaning)
        )

        Log.d(TAG, """
            사격 점수 계산:
            형격(${sagyeok.hyeong}→$hyeongNormalized): ${hyeongMeaning.character} - ${hyeongScore}점
            원격(${sagyeok.won}→$wonNormalized): ${wonMeaning.character} - ${wonScore}점
            이격(${sagyeok.i}→$iNormalized): ${iMeaning.character} - ${iScore}점
            정격(${sagyeok.jeong}→$jeongNormalized): ${jeongMeaning.character} - ${jeongScore}점
            가중 총점: ${weightedTotal}점
            조화 보너스: ${harmonyBonus}점
        """.trimIndent())

        return SagyeokScoreResult(
            hyeongScore = hyeongScore,
            wonScore = wonScore,
            iScore = iScore,
            jeongScore = jeongScore,
            weightedTotal = weightedTotal,
            harmonyBonus = harmonyBonus,
            details = mapOf(
                "형격" to hyeongMeaning,
                "원격" to wonMeaning,
                "이격" to iMeaning,
                "정격" to jeongMeaning
            )
        )
    }

    private fun normalizeStroke(stroke: Int): Int {
        return if (stroke > 81) (stroke - 1) % 80 + 1 else stroke
    }

    private fun calculateSagyeokHarmony(meanings: List<SimpleStrokeMeaning>): Int {
        var harmonyScore = 0

        // 모두 길수인 경우 보너스
        if (meanings.all { it.luck in listOf("최상운수", "상운수") }) {
            harmonyScore += 10
        }

        // 특수 조합 체크 (예: 사업운이 모두 좋은 경우)
        if (meanings.all { JsonLoader.isBusinessLuckStroke(it.strokes) }) {
            harmonyScore += 5
        }

        // 리더십 획수가 2개 이상인 경우
        if (meanings.count { JsonLoader.isLeadershipStroke(it.strokes) } >= 2) {
            harmonyScore += 5
        }

        return harmonyScore
    }

    data class SagyeokScoreResult(
        val hyeongScore: Int,
        val wonScore: Int,
        val iScore: Int,
        val jeongScore: Int,
        val weightedTotal: Int,
        val harmonyBonus: Int,
        val details: Map<String, SimpleStrokeMeaning>
    )
}