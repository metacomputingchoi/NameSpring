// model/service/AnalysisInfoGenerator.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.Sagyeok
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.NameAnalysisInfo
import com.ssc.namespring.model.data.analysis.FilteringStep
import com.ssc.namespring.model.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.data.analysis.component.YinYangAnalysisInfo
import com.ssc.namespring.model.data.analysis.component.OhaengAnalysisInfo

class AnalysisInfoGenerator(
    private val baleumOhaengCalculator: BaleumOhaengCalculator,
    private val multiOhaengHarmonyAnalyzer: MultiOhaengHarmonyAnalyzer
) {

    fun generateAnalysisInfo(
        name: GeneratedName,
        sajuInfo: SajuAnalysisInfo,
        filteringSteps: List<FilteringStep> = emptyList()
    ): NameAnalysisInfo {
        val yinYangInfo = analyzeYinYang(name)
        val ohaengInfo = analyzeOhaeng(name)
        val scoreBreakdown = calculateScoreBreakdown(name, yinYangInfo, ohaengInfo)
        val totalScore = scoreBreakdown.values.sum()
        val recommendations = generateRecommendations(sajuInfo, yinYangInfo, ohaengInfo, name)

        return NameAnalysisInfo(
            sajuInfo = sajuInfo,
            yinYangInfo = yinYangInfo,
            ohaengInfo = ohaengInfo,
            filteringSteps = filteringSteps,
            totalScore = totalScore,
            scoreBreakdown = scoreBreakdown,
            recommendations = recommendations
        )
    }

    private fun analyzeYinYang(name: GeneratedName): YinYangAnalysisInfo {
        val fullName = name.surnameHangul + name.combinedPronounciation
        val eumyangValues = fullName.mapNotNull { baleumOhaengCalculator.getBaleumEumyang(it) }
        val combinedEumyang = eumyangValues.joinToString("")

        val yinCount = eumyangValues.count { it == 0 }
        val yangCount = eumyangValues.count { it == 1 }
        val balance = if (yinCount + yangCount > 0) {
            yangCount.toFloat() / (yinCount + yangCount)
        } else 0.5f

        return YinYangAnalysisInfo(
            combinedEumyang = combinedEumyang,
            yinCount = yinCount,
            yangCount = yangCount,
            balance = balance,
            pattern = analyzeYinYangPattern(combinedEumyang),
            isBalanced = yinCount > 0 && yangCount > 0 && kotlin.math.abs(yinCount - yangCount) <= 2,
            balanceDescription = describeYinYangBalance(yinCount, yangCount)
        )
    }

    private fun analyzeOhaeng(name: GeneratedName): OhaengAnalysisInfo {
        val fullName = name.surnameHangul + name.combinedPronounciation
        val baleumOhaeng = fullName.mapNotNull { baleumOhaengCalculator.getBaleumOhaeng(it) }.joinToString("")

        val (conflictingPairs, generatingPairs) = multiOhaengHarmonyAnalyzer.analyzeOhaengRelations(baleumOhaeng)

        return OhaengAnalysisInfo(
            baleumOhaeng = baleumOhaeng,
            hoeksuOhaeng = calculateHoeksuOhaeng(name.nameHanjaHoeksu),
            jawonOhaeng = name.hanjaDetails.map { it.jawonOhaeng },
            sagyeokSuriOhaeng = calculateSagyeokSuriOhaeng(name.sagyeok),
            harmonyScore = calculateHarmonyScore(generatingPairs.size, conflictingPairs.size),
            conflictingPairs = conflictingPairs,
            generatingPairs = generatingPairs,
            overallHarmony = evaluateOverallHarmony(generatingPairs.size, conflictingPairs.size)
        )
    }

    private fun analyzeYinYangPattern(eumyang: String): String {
        return when {
            eumyang.all { it == '0' } -> "전체 음(陰)"
            eumyang.all { it == '1' } -> "전체 양(陽)"
            eumyang.count { it == '0' } > eumyang.count { it == '1' } * 2 -> "음(陰) 과다"
            eumyang.count { it == '1' } > eumyang.count { it == '0' } * 2 -> "양(陽) 과다"
            kotlin.math.abs(eumyang.count { it == '0' } - eumyang.count { it == '1' }) <= 1 -> "음양 균형"
            else -> "음양 편중"
        }
    }

    private fun describeYinYangBalance(yinCount: Int, yangCount: Int): String {
        val total = yinCount + yangCount
        return if (total > 0) {
            "음(陰) ${yinCount}개(${(yinCount * 100 / total)}%), 양(陽) ${yangCount}개(${(yangCount * 100 / total)}%)"
        } else {
            "음양 정보 없음"
        }
    }

    private fun calculateHoeksuOhaeng(hoeksu: List<Int>): List<Int> {
        return hoeksu.map { sv ->
            val ne = (sv % NamingCalculationConstants.STROKE_MODULO) +
                    (sv % NamingCalculationConstants.STROKE_MODULO) % NamingCalculationConstants.YIN_YANG_MODULO
            if (ne == NamingCalculationConstants.STROKE_MODULO) 0 else ne
        }
    }

    private fun calculateSagyeokSuriOhaeng(sagyeok: Sagyeok): List<Int> {
        return sagyeok.getValues().map { ft ->
            val te = (ft % NamingCalculationConstants.STROKE_MODULO) +
                    (ft % NamingCalculationConstants.STROKE_MODULO) % NamingCalculationConstants.YIN_YANG_MODULO
            if (te == NamingCalculationConstants.STROKE_MODULO) 0 else te
        }
    }

    private fun calculateHarmonyScore(generatingCount: Int, conflictingCount: Int): Int {
        return generatingCount * 10 - conflictingCount * 15
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
        yinYangInfo: YinYangAnalysisInfo,
        ohaengInfo: OhaengAnalysisInfo
    ): Map<String, Int> {
        return mapOf(
            "사격점수" to name.sagyeok.getValues().count { it in NamingCalculationConstants.GILHAN_HOEKSU } * 25,
            "음양균형" to if (yinYangInfo.isBalanced) 20 else 0,
            "오행조화" to ohaengInfo.harmonyScore,
            "획수길흉" to name.nameHanjaHoeksu.count { it in NamingCalculationConstants.GILHAN_HOEKSU } * 10
        )
    }

    private fun generateRecommendations(
        sajuInfo: SajuAnalysisInfo,
        yinYangInfo: YinYangAnalysisInfo,
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
        if (yinYangInfo.isBalanced) {
            recommendations.add("음양이 조화롭게 균형을 이루고 있습니다.")
        } else {
            val dominant = if (yinYangInfo.yinCount > yinYangInfo.yangCount) "음(陰)" else "양(陽)"
            recommendations.add("$dominant 기운이 강하므로 반대 기운을 보완하는 것이 좋습니다.")
        }

        // 오행 조화
        if (ohaengInfo.overallHarmony == "매우 조화로움" || ohaengInfo.overallHarmony == "조화로움") {
            recommendations.add("오행이 서로 상생하여 조화롭습니다.")
        }

        // 길한 획수
        val gilhanCount = name.sagyeok.getValues().count { it in NamingCalculationConstants.GILHAN_HOEKSU }
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
