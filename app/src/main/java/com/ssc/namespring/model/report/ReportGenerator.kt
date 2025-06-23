// model/report/ReportGenerator.kt
package com.ssc.namespring.model.report

import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.calculator.ScoreCalculator
import com.ssc.namespring.model.constants.Constants

class ReportGenerator {

    fun generateNameExplanation(result: NameResult, scoreCalculator: ScoreCalculator): Pair<NameExplanation, NameScore> {
        val scores = scoreCalculator.calculateDetailedScore(result)
        val fullName = "${result.surHangul}${result.combinedPronounciation}"
        val fullHanja = "${result.surHanja}${result.combinedHanja}"

        // 1. 요약
        val grade = when {
            scores.total >= Constants.GRADE_A_THRESHOLD -> "A"
            scores.total >= Constants.GRADE_B_THRESHOLD -> "B"
            scores.total >= Constants.GRADE_C_THRESHOLD -> "C"
            else -> "D"
        }
        val summary = "'$fullName($fullHanja)'는 총점 ${scores.total}점(${Constants.MAX_SCORE}점 만점)으로 ${grade}등급의 이름입니다."

        // 2. 사주 분석
        val elementCount = result.dictElementsCount.toMap()
        var sajuAnalysis = """생년월일시를 기준으로 한 사주팔자는 ${result.sajuInfo.yeonju}년주, ${result.sajuInfo.wolju}월주, ${result.sajuInfo.ilju}일주, ${result.sajuInfo.siju}시주입니다.
오행 분포는 목(${elementCount["木"]}), 화(${elementCount["火"]}), 토(${elementCount["土"]}), 금(${elementCount["金"]}), 수(${elementCount["水"]})로 구성되어 있습니다."""

        if (result.zeroElements.isNotEmpty()) {
            sajuAnalysis += "\n사주에서 부족한 ${result.zeroElements.joinToString(", ")} 오행을 이름으로 보완하는 것이 중요합니다."
        } else if (result.oneElements.isNotEmpty()) {
            sajuAnalysis += "\n사주에서 상대적으로 약한 ${result.oneElements.joinToString(", ")} 오행을 이름으로 보강하는 것이 좋습니다."
        }

        // 3. 획수 분석
        val fourTypes = result.combinationAnalysis.fourTypes
        val fourTypesNames = listOf("인격(人格)", "지격(地格)", "외격(外格)", "총격(總格)")
        val strokeDetails = fourTypes.mapIndexed { i, value ->
            val luckStatus = if (result.combinationAnalysis.fourTypesLuck[i] == 1) "길수" else "흉수"
            "${ fourTypesNames[i]} ${value}획($luckStatus)"
        }

        val strokeAnalysis = """사격(四格) 수리는 ${strokeDetails.joinToString(", ")}로 구성되어 있습니다.
길수 개수: ${result.combinationAnalysis.fourTypesLuck.sum()}개/4개"""

        // 4. 오행 조화 분석
        var elementHarmony = "이름의 오행 구성은 ${result.combinedElement}입니다. "

        val harmonyCheck = result.filteringProcess.find { it.step == "element_harmony_check" }
        if (harmonyCheck != null && harmonyCheck.details != null) {
            @Suppress("UNCHECKED_CAST")
            val harmonyDetails = harmonyCheck.details["harmony_details"] as List<Map<String, Any>>
            harmonyDetails.forEach { detail ->
                when (detail["relation"]) {
                    "harmonious" -> elementHarmony += "${detail["elements"]}는 상생 관계로 좋은 기운이 흐릅니다. "
                    else -> elementHarmony += "${detail["elements"]}는 상극 관계로 주의가 필요합니다. "
                }
            }
        }

        // 5. 음양 균형 분석
        val pmPattern = result.combinedPm!!
        val yinCount = pmPattern.count { it == Constants.YIN_COUNT_INDEX }
        val yangCount = pmPattern.count { it == Constants.YANG_COUNT_INDEX }

        val yinYangBalance = """음양 구성은 '$pmPattern'로 음($yinCount)과 양($yangCount)의 비율입니다.
${if (pmPattern.toSet().size == Constants.YIN_YANG_SET_SIZE) "균형잡힌 음양 배치로 조화롭습니다." else "음양이 치우쳐 있어 균형이 필요합니다."}"""

        // 6. 발음 분석
        val pronunciationAnalysis = "'${result.combinedPronounciation}'는 " +
                if (result.filteringProcess.any { it.step == "hangul_naturalness_check" && it.passed }) {
                    "자연스럽고 부르기 쉬운 발음입니다."
                } else {
                    "다소 어색한 발음 조합입니다."
                }

        // 7. 종합 평가
        val strengths = mutableListOf<String>()
        val weaknesses = mutableListOf<String>()

        if (scores.fourTypesLuck >= Constants.SCORE_HIGH_THRESHOLD_1) strengths.add("사격 수리가 대체로 길하여 운세가 좋습니다")
        if (scores.sajuComplement >= Constants.SCORE_HIGH_THRESHOLD_2) strengths.add("사주의 부족한 오행을 잘 보완합니다")
        if (scores.nameElementHarmony >= Constants.SCORE_HIGH_THRESHOLD_3) strengths.add("삼원오행이 조화롭게 상생합니다")
        if (scores.yinYangBalance >= Constants.SCORE_HIGH_THRESHOLD_1) strengths.add("음양이 균형잡혀 있습니다")

        if (scores.fourTypesLuck < Constants.SCORE_LOW_THRESHOLD_1) weaknesses.add("사격 수리에 흉수가 많습니다")
        if (scores.nameElementHarmony < Constants.SCORE_LOW_THRESHOLD_2) weaknesses.add("오행 간 상극이 존재합니다")
        if (scores.yinYangBalance == Constants.SCORE_LOW_THRESHOLD_2) weaknesses.add("음양 균형이 맞지 않습니다")

        val overallEvaluation = """[장점]
${if (strengths.isNotEmpty()) strengths.joinToString("\n") { "• $it" } else "• 특별한 장점을 찾기 어렵습니다."}

[단점]
${if (weaknesses.isNotEmpty()) weaknesses.joinToString("\n") { "• $it" } else "• 특별한 단점이 없습니다."}"""

        // 8. 추천사항
        val recommendations = mutableListOf<String>()
        when {
            scores.total >= Constants.GRADE_A_THRESHOLD -> recommendations.add("매우 좋은 이름으로 적극 추천합니다.")
            scores.total >= Constants.GRADE_B_THRESHOLD -> recommendations.add("좋은 이름으로 사용하기에 무난합니다.")
            scores.total >= Constants.GRADE_C_THRESHOLD -> recommendations.add("보통 수준의 이름으로 신중한 검토가 필요합니다.")
            else -> recommendations.add("다른 이름을 고려해보시는 것을 권합니다.")
        }

        val explanation = NameExplanation(
            summary, sajuAnalysis, strokeAnalysis, elementHarmony,
            yinYangBalance, pronunciationAnalysis, overallEvaluation, recommendations
        )

        return explanation to scores
    }

    fun printDetailedNameReport(result: NameResult, scoreCalculator: ScoreCalculator) {
        println("\n${"=".repeat(Constants.REPORT_SEPARATOR_LENGTH)}")
        println("【 ${result.surHangul}${result.combinedPronounciation}(${result.surHanja}${result.combinedHanja}) 성명학 분석 보고서 】")
        println("=".repeat(Constants.REPORT_SEPARATOR_LENGTH))

        val (explanation, scores) = generateNameExplanation(result, scoreCalculator)

        // 1. 요약 정보
        println("\n▶ 종합 평가: ${explanation.summary}")
        println("\n▶ 점수 상세:")
        println("   • 사격 수리 길흉: ${scores.fourTypesLuck}점/10점")
        println("   • 삼원오행 조화: ${scores.nameElementHarmony}점/15점")
        println("   • 사격오행 조화: ${scores.typeElementHarmony}점/15점")
        println("   • 음양 균형: ${scores.yinYangBalance}점/10점")
        println("   • 사주 보완: ${scores.sajuComplement}점/20점")
        println("   • 발음 조화: ${scores.pronunciation}점/10점")
        println("   • 의미 조화: ${scores.meaning}점/10점")
        println("   ─────────────────────────")
        println("   • 총점: ${scores.total}점/${Constants.TOTAL_SCORE_MAX}점")

        // 2. 사주 분석
        println("\n▶ 사주 분석:")
        println(explanation.sajuAnalysis)

        // 3. 획수 분석
        println("\n▶ 획수 분석:")
        println(explanation.strokeAnalysis)

        // 4. 오행 조화
        println("\n▶ 오행 조화:")
        println(explanation.elementHarmony)

        // 5. 음양 균형
        println("\n▶ 음양 균형:")
        println(explanation.yinYangBalance)

        // 6. 발음 분석
        println("\n▶ 발음 분석:")
        println(explanation.pronunciationAnalysis)

        // 7. 종합 평가
        println("\n▶ 종합 평가:")
        println(explanation.overallEvaluation)

        // 8. 추천사항
        println("\n▶ 추천사항:")
        explanation.recommendations.forEach { println("   • $it") }

        // 9. 한자 상세 정보
        println("\n▶ 한자 상세 정보:")
        println("   • ${result.hanja1Info.hanja}(${result.hanja1Info.inmyeongYongEum}): ${result.hanja1Info.inmyeongYongDdeut}")
        println("     - 자원오행: ${result.hanja1Info.jawonOheng}, 발음오행: ${result.hanja1Info.baleumOheng}")
        result.hanja1Info.cautionRed?.let { println("     - 주의: $it") }

        println("   • ${result.hanja2Info.hanja}(${result.hanja2Info.inmyeongYongEum}): ${result.hanja2Info.inmyeongYongDdeut}")
        println("     - 자원오행: ${result.hanja2Info.jawonOheng}, 발음오행: ${result.hanja2Info.baleumOheng}")
        result.hanja2Info.cautionBlue?.let { println("     - 참고: $it") }

        println("\n${"=".repeat(Constants.REPORT_SEPARATOR_LENGTH)}")
    }
}