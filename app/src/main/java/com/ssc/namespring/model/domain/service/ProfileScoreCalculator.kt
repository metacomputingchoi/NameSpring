// model/domain/service/ProfileScoreCalculator.kt
package com.ssc.namespring.model.domain.service

import android.util.Log
import com.ssc.namingengine.data.GeneratedName

object ProfileScoreCalculator {
    fun calculateNamebomScore(generatedName: GeneratedName): Int {
        val analysisInfo = generatedName.analysisInfo ?: return 0
        val scoreBreakdown = analysisInfo.scoreBreakdown

        Log.d("Profile", "=== 점수 계산 시작 ===")
        Log.d("Profile", "총점(totalScore): ${analysisInfo.totalScore}")
        Log.d("Profile", "받은 점수들:")
        scoreBreakdown.forEach { (category, score) ->
            Log.d("Profile", "  $category: $score")
        }

        var ohaengBonus = 0
        try {
            val jawonOhaeng = analysisInfo.ohaengInfo.jawonOhaeng
            val missingElements = analysisInfo.sajuInfo.missingElements
            val dominantElements = analysisInfo.sajuInfo.dominantElements
            val sajuOhaengCount = analysisInfo.sajuInfo.sajuOhaengCount

            Log.d("Profile", "자원오행: $jawonOhaeng")
            Log.d("Profile", "부족한 오행: $missingElements")
            Log.d("Profile", "과다한 오행: $dominantElements")
            Log.d("Profile", "사주 오행 분포: $sajuOhaengCount")

            jawonOhaeng.forEach { element ->
                if (missingElements.contains(element)) {
                    ohaengBonus += 10
                    Log.d("Profile", "부족한 오행 $element 보완 -> +10점")
                } else if (dominantElements.contains(element)) {
                    ohaengBonus -= 5
                    Log.d("Profile", "과다한 오행 $element 추가 -> -5점")
                } else {
                    val count = sajuOhaengCount[element] ?: 0
                    if (count <= 1) {
                        ohaengBonus += 5
                        Log.d("Profile", "적은 오행 $element 보완 -> +5점")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Profile", "자원오행 분석 실패", e)
        }

        val baseScore = (analysisInfo.totalScore * 100 / 160).coerceIn(0, 100)
        val ohaengScore = scoreBreakdown["오행조화"] ?: 0
        if (ohaengScore == 0) {
            ohaengBonus = (ohaengBonus * 1.5).toInt()
            Log.d("Profile", "오행조화 점수가 0이므로 자원오행 보너스 1.5배 적용")
        }

        val finalScore = (baseScore + ohaengBonus).coerceIn(0, 100)

        Log.d("Profile", "기본 점수: $baseScore")
        Log.d("Profile", "자원오행 보너스: $ohaengBonus")
        Log.d("Profile", "최종 점수: $finalScore")
        Log.d("Profile", "=== 점수 계산 끝 ===")

        return finalScore
    }
}