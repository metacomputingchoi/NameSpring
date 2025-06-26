// model/service/AnalysisInfoGenerator.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.naming.NamingCalculationConstants.ScoreConstants
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.NameAnalysisInfo
import com.ssc.namespring.model.data.analysis.FilteringStep
import com.ssc.namespring.model.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.data.analysis.component.EumYangAnalysisInfo
import com.ssc.namespring.model.data.analysis.component.OhaengAnalysisInfo
import com.ssc.namespring.model.util.NamingCalculationUtils
import com.ssc.namespring.model.util.OhaengCalculationUtils

class AnalysisInfoGenerator(
    private val baleumOhaengCalculator: BaleumOhaengCalculator,
    private val multiOhaengHarmonyAnalyzer: MultiOhaengHarmonyAnalyzer
) {
    private val eumYangAnalysisService = EumYangAnalysisService()

    fun generateAnalysisInfo(
        name: GeneratedName,
        sajuInfo: SajuAnalysisInfo,
        filteringSteps: List<FilteringStep> = emptyList()
    ): NameAnalysisInfo {
        val eumYangInfo = analyzeEumYang(name)
        val ohaengInfo = analyzeOhaeng(name)
        val scoreBreakdown = calculateScoreBreakdown(name, eumYangInfo, ohaengInfo)
        val totalScore = scoreBreakdown.values.sum()
        val recommendations = generateRecommendations(sajuInfo, eumYangInfo, ohaengInfo, name)

        return NameAnalysisInfo(
            sajuInfo = sajuInfo,
            eumYangInfo = eumYangInfo,
            ohaengInfo = ohaengInfo,
            filteringSteps = filteringSteps,
            totalScore = totalScore,
            scoreBreakdown = scoreBreakdown,
            recommendations = recommendations
        )
    }

    private fun analyzeEumYang(name: GeneratedName): EumYangAnalysisInfo {
        val fullName = name.surnameHangul + name.combinedPronounciation
        val eumyangValues = fullName.mapNotNull { baleumOhaengCalculator.getBaleumEumyang(it) }
        return eumYangAnalysisService.analyzeEumYang(eumyangValues)
    }

    private fun analyzeOhaeng(name: GeneratedName): OhaengAnalysisInfo {
        val fullName = name.surnameHangul + name.combinedPronounciation
        val baleumOhaeng = fullName.mapNotNull { baleumOhaengCalculator.getBaleumOhaeng(it) }.joinToString("")

        val (conflictingPairs, generatingPairs) = multiOhaengHarmonyAnalyzer.analyzeOhaengRelations(baleumOhaeng)

        return OhaengAnalysisInfo(
            baleumOhaeng = baleumOhaeng,
            hoeksuOhaeng = OhaengCalculationUtils.calculateHoeksuListToOhaeng(name.nameHanjaHoeksu),
            jawonOhaeng = name.hanjaDetails.map { it.jawonOhaeng },
            sagyeokSuriOhaeng = OhaengCalculationUtils.calculateHoeksuListToOhaeng(name.sagyeok.getValues()),
            harmonyScore = calculateHarmonyScore(generatingPairs.size, conflictingPairs.size),
            conflictingPairs = conflictingPairs,
            generatingPairs = generatingPairs,
            overallHarmony = evaluateOverallHarmony(generatingPairs.size, conflictingPairs.size)
        )
    }

    private fun calculateHarmonyScore(generatingCount: Int, conflictingCount: Int): Int {
        return generatingCount * ScoreConstants.HARMONY_GENERATING_SCORE -
                conflictingCount * ScoreConstants.HARMONY_CONFLICTING_PENALTY
    }

    private fun evaluateOverallHarmony(generatingCount: Int, conflictingCount: Int): String {
        return when {
            conflictingCount == 0 && generatingCount >= 2 -> "매우 조화로움"
            conflictingCount == 0 && generatingCount >= 1 -> "조화로움"
            conflictingCount == 1 && generatingCount >= 2 -> "대체로 조화로움"
            conflictingCount >= 2 -> "부조화"
            else -> "보통"
        }
    }

    private fun calculateScoreBreakdown(
        name: GeneratedName,
        eumYangInfo: EumYangAnalysisInfo,
        ohaengInfo: OhaengAnalysisInfo
    ): Map<String, Int> {
        return mapOf(
            "사격점수" to NamingCalculationUtils.countGilhanHoeksu(name.sagyeok.getValues()) *
                    ScoreConstants.SAGYEOK_SCORE_MULTIPLIER,
            "음양균형" to if (eumYangInfo.isBalanced) ScoreConstants.YIN_YANG_BALANCE_SCORE else 0,
            "오행조화" to ohaengInfo.harmonyScore,
            "획수길흉" to NamingCalculationUtils.countGilhanHoeksu(name.nameHanjaHoeksu) *
                    ScoreConstants.HOEKSU_GILHAN_SCORE
        )
    }

    private fun generateRecommendations(
        sajuInfo: SajuAnalysisInfo,
        eumYangInfo: EumYangAnalysisInfo,
        ohaengInfo: OhaengAnalysisInfo,
        name: GeneratedName
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // 사주 보완
        if (sajuInfo.missingElements.isNotEmpty()) {
            val supplemented = sajuInfo.missingElements.filter { element ->
                name.hanjaDetails.any { it.jawonOhaeng == element }
            }
            if (supplemented.isNotEmpty()) {
                recommendations.add("사주에 없는 ${supplemented.joinToString(", ")} 오행을 보완합니다.")
            }
        }

        // 음양 균형
        if (eumYangInfo.isBalanced) {
            recommendations.add("음양이 조화롭게 균형을 이루고 있습니다.")
        } else {
            val dominant = if (eumYangInfo.eumCount > eumYangInfo.yangCount) "음(陰)" else "양(陽)"
            recommendations.add("$dominant 기운이 강하므로 반대 기운을 보완하는 것이 좋습니다.")
        }

        // 오행 조화
        if (ohaengInfo.overallHarmony == "매우 조화로움" || ohaengInfo.overallHarmony == "조화로움") {
            recommendations.add("오행이 서로 상생하여 조화롭습니다.")
        }

        // 길한 획수
        val gilhanCount = NamingCalculationUtils.countGilhanHoeksu(name.sagyeok.getValues())
        if (gilhanCount >= 3) {
            recommendations.add("사격 중 ${gilhanCount}개가 길한 수로 매우 좋습니다.")
        }

        // 한자 의미
        val meanings = name.hanjaDetails.map { it.inmyongMeaning }.filter { it.isNotEmpty() }
        if (meanings.isNotEmpty()) {
            recommendations.add("이름의 뜻: ${meanings.joinToString(" + ")}")
        }

        return recommendations
    }
}