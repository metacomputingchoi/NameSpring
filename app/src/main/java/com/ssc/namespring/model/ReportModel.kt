// model/ReportModel.kt
package com.ssc.namespring.model

import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.repository.ReportRepository
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namingengine.data.analysis.NameAnalysisInfo
import java.time.LocalDateTime
import java.util.UUID

/**
 * 평가 보고서 생성 비즈니스 로직
 * GeneratedName의 analysisInfo를 해석하여 보고서 생성
 */
class ReportModel(
    private val repository: ReportRepository
) {

    suspend fun generateEvaluationReport(
        evaluatedName: GeneratedName,
        profile: Profile
    ): Result<EvaluationReport> {
        return try {
            val analysisInfo = evaluatedName.analysisInfo
                ?: return Result.failure(Exception("분석 정보가 없습니다"))

            // 1. 사주 보완도 평가 (이미 계산된 정보 활용)
            val sajuCompensation = evaluateSajuCompensation(evaluatedName, profile)

            // 2. 음양 균형도 평가 (analysisInfo의 eumYangInfo 활용)
            val yinYangBalance = evaluateYinYangBalance(analysisInfo)

            // 3. 오행 조화도 평가 (analysisInfo의 ohaengInfo 활용)
            val fiveElementsHarmony = evaluateFiveElementsHarmony(analysisInfo)

            // 4. 획수 길흉 평가 (sagyeok 정보와 scoreBreakdown 활용)
            val strokeAuspiciousness = evaluateStrokeAuspiciousness(evaluatedName, analysisInfo)

            // 5. 발음 자연스러움 평가 (filteringSteps 활용)
            val pronunciationNaturalness = evaluatePronunciationNaturalness(analysisInfo)

            // 종합 점수는 이미 계산된 이름봄 점수 활용
            val overallScore = calculateNamebomScore(analysisInfo)

            // 추천사항과 개선사항은 analysisInfo의 recommendations 활용
            val recommendations = extractRecommendations(analysisInfo, 
                sajuCompensation, yinYangBalance, fiveElementsHarmony, 
                strokeAuspiciousness, pronunciationNaturalness)

            val improvements = extractImprovements(
                sajuCompensation, yinYangBalance, fiveElementsHarmony, 
                strokeAuspiciousness, pronunciationNaturalness)

            val report = EvaluationReport(
                id = UUID.randomUUID().toString(),
                evaluatedName = evaluatedName,
                profile = profile,
                overallScore = overallScore,
                sajuCompensation = sajuCompensation,
                yinYangBalance = yinYangBalance,
                fiveElementsHarmony = fiveElementsHarmony,
                strokeAuspiciousness = strokeAuspiciousness,
                pronunciationNaturalness = pronunciationNaturalness,
                recommendations = recommendations,
                improvements = improvements
            )

            repository.saveEvaluationReport(report)
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
    * 사주 보완도 평가
    * GeneratedName과 Profile의 사주 정보를 비교하여 보완 정도 평가
    */
    private fun evaluateSajuCompensation(name: GeneratedName, profile: Profile): ScoreDetail {
        val nameSajuInfo = name.analysisInfo?.sajuInfo
        val profileSajuInfo = profile.sajuInfo

        if (nameSajuInfo == null || profileSajuInfo == null) {
            return ScoreDetail(
                score = 50,
                description = "사주 정보가 불완전합니다",
                analysis = "사주 분석이 필요합니다",
                level = ScoreLevel.AVERAGE
            )
        }

        // 이름의 오행이 사주의 부족한 오행을 보완하는지 확인
        val nameOhaeng = name.analysisInfo?.ohaengInfo?.baleumOhaeng ?: ""
        val jawonOhaeng = name.analysisInfo?.ohaengInfo?.jawonOhaeng ?: emptyList()
        val missingElements = profileSajuInfo.missingElements

        var score = 70 // 기본 점수
        var compensationCount = 0
        val compensatedElements = mutableListOf<String>()

        // 발음 오행과 자원 오행 모두 확인
        val allNameOhaeng = nameOhaeng.toList().map { it.toString() } + jawonOhaeng

        missingElements.forEach { element ->
            if (allNameOhaeng.contains(element)) {
                score += 10
                compensationCount++
                compensatedElements.add(element)
            }
        }

        // 과다한 오행을 더 늘리지 않는지 확인
        val dominantElements = profileSajuInfo.dominantElements
        dominantElements.forEach { element ->
            if (allNameOhaeng.count { it == element } >= 2) {
                score -= 5
            }
        }

        val finalScore = score.coerceIn(0, 100)

        val description = when {
            compensationCount >= 2 -> "사주의 부족한 오행을 훌륭히 보완합니다"
            compensationCount == 1 -> "사주의 부족한 오행을 일부 보완합니다"
            missingElements.isEmpty() -> "사주가 균형잡혀 있어 보완이 필요하지 않습니다"
            else -> "사주 보완 효과가 제한적입니다"
        }

        val analysis = buildString {
            append("사주에 부족한 오행: ${missingElements.joinToString(", ").ifEmpty { "없음" }}\n")
            append("이름이 보완하는 오행: ${compensatedElements.joinToString(", ").ifEmpty { "없음" }}\n")
            append("이름의 오행 구성: 발음(${nameOhaeng}), 자원(${jawonOhaeng.joinToString("")})")
        }

        return ScoreDetail(finalScore, description, analysis,
            ScoreLevel.values().first { finalScore in it.range })
    }

    /**
     * 음양 균형도 평가
     * analysisInfo의 eumYangInfo를 직접 활용
     */
    private fun evaluateYinYangBalance(analysisInfo: NameAnalysisInfo): ScoreDetail {
        val eumYangInfo = analysisInfo.eumYangInfo

        // 이미 계산된 균형 정보 활용
        val score = when {
            eumYangInfo.isBalanced && eumYangInfo.balance in 0.4..0.6 -> 95
            eumYangInfo.isBalanced -> 85
            eumYangInfo.balance in 0.3..0.7 -> 70
            eumYangInfo.balance in 0.2..0.8 -> 50
            else -> 30
        }

        val description = when {
            score >= 90 -> "음양이 완벽한 균형을 이룹니다"
            score >= 70 -> "음양 균형이 양호합니다"
            score >= 50 -> "음양 균형이 보통입니다"
            else -> "음양 균형 개선이 필요합니다"
        }

        val analysis = buildString {
            append(eumYangInfo.balanceDescription).append("\n")
            append("패턴: ${eumYangInfo.pattern}\n")
            append("균형도: ${(eumYangInfo.balance * 100).toInt()}%")
        }

        return ScoreDetail(score, description, analysis,
            ScoreLevel.values().first { score in it.range })
    }

    /**
     * 오행 조화도 평가
     * analysisInfo의 ohaengInfo를 직접 활용
     */
    private fun evaluateFiveElementsHarmony(analysisInfo: NameAnalysisInfo): ScoreDetail {
        val ohaengInfo = analysisInfo.ohaengInfo

        // 이미 계산된 조화도 정보 활용
        val score = when (ohaengInfo.overallHarmony) {
            "매우 조화로움" -> 95
            "조화로움" -> 75
            "보통" -> 55
            "부조화" -> 35
            else -> 20
        }

        val description = ohaengInfo.overallHarmony

        val analysis = buildString {
            append("발음 오행: ${ohaengInfo.baleumOhaeng}\n")
            append("자원 오행: ${ohaengInfo.jawonOhaeng.joinToString(", ")}")

            if (ohaengInfo.generatingPairs.isNotEmpty()) {
                append("\n상생 관계: ${ohaengInfo.generatingPairs.joinToString(", ") {
                    "${it.first}→${it.second}"
                }}")
            }

            if (ohaengInfo.conflictingPairs.isNotEmpty()) {
                append("\n상극 관계: ${ohaengInfo.conflictingPairs.joinToString(", ") {
                    "${it.first}⇒${it.second}"
                }}")
            }
        }

        return ScoreDetail(score, description, analysis.trim(),
            ScoreLevel.values().first { score in it.range })
    }

    /**
     * 획수 길흉 평가
     * sagyeok 정보와 scoreBreakdown의 사격점수 활용
     */
    private fun evaluateStrokeAuspiciousness(
        name: GeneratedName,
        analysisInfo: NameAnalysisInfo
    ): ScoreDetail {
        val sagyeok = name.sagyeok
        val sagyeokScore = analysisInfo.scoreBreakdown["사격점수"] ?: 0

        // 사격점수를 100점 만점으로 변환
        val score = when {
            sagyeokScore >= 100 -> 95  // 4개 모두 길수
            sagyeokScore >= 75 -> 80   // 3개 길수
            sagyeokScore >= 50 -> 65   // 2개 길수
            sagyeokScore >= 25 -> 45   // 1개 길수
            else -> 25                 // 0개 길수
        }

        val auspiciousCount = sagyeokScore / 25  // 각 길수당 25점으로 계산

        val description = when (auspiciousCount) {
            4 -> "모든 사격이 길한 수로 최상의 획수입니다"
            3 -> "대부분의 사격이 길한 수입니다"
            2 -> "절반의 사격이 길한 수입니다"
            1 -> "일부 사격이 길한 수입니다"
            else -> "길한 획수가 부족합니다"
        }

        val analysis = buildString {
            append("형격(${sagyeok.hyeong}), ")
            append("원격(${sagyeok.won}), ")
            append("이격(${sagyeok.i}), ")
            append("정격(${sagyeok.jeong})\n")
            append("길한 사격: ${auspiciousCount}개/4개")
        }

        return ScoreDetail(score, description, analysis,
            ScoreLevel.values().first { score in it.range })
    }

    /**
     * 발음 자연스러움 평가
     * filteringSteps 정보 활용
     */
    private fun evaluatePronunciationNaturalness(analysisInfo: NameAnalysisInfo): ScoreDetail {
        val pronunciationStep = analysisInfo.filteringSteps
            .find { it.filterName.contains("발음") }

        val score = if (pronunciationStep?.passed == true) {
            85  // 발음 필터 통과
        } else {
            40  // 발음 필터 미통과 또는 정보 없음
        }

        val description = if (score >= 80) {
            "발음이 자연스럽고 부르기 편합니다"
        } else {
            "발음이 다소 어색할 수 있습니다"
        }

        val analysis = pronunciationStep?.reason ?: "발음 평가 정보가 없습니다"

        return ScoreDetail(score, description, analysis,
            ScoreLevel.values().first { score in it.range })
    }

    /**
     * 이름봄 점수 계산
     * analysisInfo의 totalScore와 scoreBreakdown 활용
     */
    private fun calculateNamebomScore(analysisInfo: NameAnalysisInfo): Int {
        val scoreBreakdown = analysisInfo.scoreBreakdown

        // 각 항목을 100점 만점으로 정규화하여 가중 평균
        val normalizedScores = mapOf(
            "사격점수" to (scoreBreakdown["사격점수"] ?: 0) * 1.0,  // 이미 100점 만점
            "음양균형" to (scoreBreakdown["음양균형"] ?: 0) * 5.0,  // 20점 -> 100점
            "오행조화" to (scoreBreakdown["오행조화"] ?: 0) * 5.0,  // 20점 -> 100점
            "획수길흉" to (scoreBreakdown["획수길흉"] ?: 0) * 5.0   // 20점 -> 100점
        )

        val weights = mapOf(
            "사격점수" to 0.35,
            "음양균형" to 0.25,
            "오행조화" to 0.25,
            "획수길흉" to 0.15
        )

        val weightedSum = normalizedScores.entries.sumOf { (key, score) ->
            score * (weights[key] ?: 0.0)
        }

        return weightedSum.toInt().coerceIn(0, 100)
    }

    /**
     * 추천사항 추출
     */
    private fun extractRecommendations(
        analysisInfo: NameAnalysisInfo,
        vararg details: ScoreDetail
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // analysisInfo의 기존 추천사항 추가
        recommendations.addAll(analysisInfo.recommendations)

        // 높은 점수 항목들의 설명 추가
        details.forEach { detail ->
            if (detail.level == ScoreLevel.EXCELLENT || detail.level == ScoreLevel.GOOD) {
                recommendations.add(detail.description)
            }
        }

        return recommendations.distinct()
    }

    /**
     * 개선사항 추출
     */
    private fun extractImprovements(vararg details: ScoreDetail): List<String> {
        val improvements = mutableListOf<String>()

        details.forEach { detail ->
            if (detail.level == ScoreLevel.BELOW || detail.level == ScoreLevel.POOR) {
                improvements.add(detail.analysis)
            }
        }

        return improvements
    }

    suspend fun generateComparisonReport(
        names: List<GeneratedName>,
        profile: Profile
    ): Result<ComparisonReport> {
        return try {
            require(names.size in 2..4) { "비교는 2~4개의 이름만 가능합니다" }

            val comparisonResults = mutableMapOf<String, List<ComparisonScore>>()
            val categories = listOf("사주보완", "음양균형", "오행조화", "획수길흉", "발음자연", "종합점수")

            // 각 카테고리별로 점수 계산 및 순위 매기기
            categories.forEach { category ->
                val scores = names.map { name ->
                    val score = when (category) {
                        "사주보완" -> evaluateSajuCompensation(name, profile).score
                        "음양균형" -> name.analysisInfo?.let { evaluateYinYangBalance(it).score } ?: 0
                        "오행조화" -> name.analysisInfo?.let { evaluateFiveElementsHarmony(it).score } ?: 0
                        "획수길흉" -> name.analysisInfo?.let { evaluateStrokeAuspiciousness(name, it).score } ?: 0
                        "발음자연" -> name.analysisInfo?.let { evaluatePronunciationNaturalness(it).score } ?: 0
                        "종합점수" -> name.analysisInfo?.let { calculateNamebomScore(it) } ?: 0
                        else -> 0
                    }
                    ComparisonScore(name, score, 0)
                }.sortedByDescending { it.score }.mapIndexed { index, score ->
                    score.copy(rank = index + 1)
                }

                comparisonResults[category] = scores
            }

            // 종합 순위 계산
            val totalScores = names.map { name ->
                val total = categories.sumOf { category ->
                    comparisonResults[category]?.find { it.name == name }?.score ?: 0
                }
                name to total
            }.sortedByDescending { it.second }

            val rankings = totalScores.mapIndexed { index, (name, score) ->
                val strengths = mutableListOf<String>()
                val weaknesses = mutableListOf<String>()

                comparisonResults.forEach { (category, scores) ->
                    val nameScore = scores.find { it.name == name }
                    if (nameScore != null) {
                        if (nameScore.rank == 1) strengths.add(category)
                        if (nameScore.rank == names.size) weaknesses.add(category)
                    }
                }

                RankingResult(
                    rank = index + 1,
                    name = name,
                    totalScore = score / categories.size,
                    strengths = strengths,
                    weaknesses = weaknesses
                )
            }

            val report = ComparisonReport(
                id = UUID.randomUUID().toString(),
                profile = profile,
                comparedNames = names,
                comparisonResults = comparisonResults,
                rankings = rankings,
                winnerName = rankings.first().name
            )

            repository.saveComparisonReport(report)
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportToPDF(reportId: String): Result<String> {
        // PDF 생성 로직은 별도 유틸리티에서 구현
        return Result.success("report_$reportId.pdf")
    }

    suspend fun shareReport(reportId: String): Result<Boolean> {
        // 공유 로직은 별도 유틸리티에서 구현
        return Result.success(true)
    }
}

// Extension property for ScoreLevel range
private val ScoreLevel.range: IntRange
    get() = when (this) {
        ScoreLevel.EXCELLENT -> 80..100
        ScoreLevel.GOOD -> 60..79
        ScoreLevel.AVERAGE -> 40..59
        ScoreLevel.BELOW -> 20..39
        ScoreLevel.POOR -> 0..19
    }