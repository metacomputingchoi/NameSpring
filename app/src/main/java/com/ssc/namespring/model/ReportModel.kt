// model/ReportModel.kt
package com.ssc.namespring.model

import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.repository.ReportRepository
import com.ssc.namespring.utils.JsonLoader
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namingengine.data.analysis.NameAnalysisInfo
import java.time.LocalDateTime
import java.util.UUID

/**
 * 평가 보고서 생성 비즈니스 로직
 * JSON 데이터를 활용하여 더 풍부한 보고서 생성
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

            // JSON 데이터를 활용한 평가
            val sajuCompensation = evaluateSajuCompensationWithJson(evaluatedName, profile)
            val yinYangBalance = evaluateYinYangBalanceWithJson(analysisInfo)
            val fiveElementsHarmony = evaluateFiveElementsHarmonyWithJson(analysisInfo)
            val strokeAuspiciousness = evaluateStrokeAuspiciousnessWithJson(evaluatedName, analysisInfo)
            val pronunciationNaturalness = evaluatePronunciationNaturalnessWithJson(analysisInfo)

            // 종합 점수는 이미 계산된 이름봄 점수 활용
            val overallScore = calculateNamebomScore(analysisInfo)

            // JSON 데이터를 활용한 추천사항과 개선사항
            val recommendations = extractRecommendationsWithJson(analysisInfo, overallScore,
                sajuCompensation, yinYangBalance, fiveElementsHarmony,
                strokeAuspiciousness, pronunciationNaturalness)

            val improvements = extractImprovementsWithJson(overallScore,
                sajuCompensation, yinYangBalance, fiveElementsHarmony,
                strokeAuspiciousness, pronunciationNaturalness)

            // 성격 분석 추가 (stroke_meanings.json 활용)
            val personalityAnalysis = analyzePersonalityWithJson(evaluatedName)

            // 적합 직업 분석 추가
            val careerGuidance = analyzeCareerWithJson(evaluatedName, analysisInfo)

            // 인생 시기별 분석 추가
            val lifePeriodAnalysis = analyzeLifePeriodsWithJson(evaluatedName)

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
                improvements = improvements,
                personalityAnalysis = personalityAnalysis,
                careerGuidance = careerGuidance,
                lifePeriodAnalysis = lifePeriodAnalysis
            )

            repository.saveEvaluationReport(report)
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * JSON 데이터를 활용한 사주 보완도 평가
     */
    private fun evaluateSajuCompensationWithJson(name: GeneratedName, profile: Profile): ScoreDetail {
        val nameSajuInfo = name.analysisInfo?.sajuInfo
        val profileSajuInfo = profile.sajuInfo

        if (nameSajuInfo == null || profileSajuInfo == null) {
            return ScoreDetail(
                score = 50,
                description = JsonLoader.sajuAnalyzerStrings.defaultValues["analysis_error"] ?: "분석 불가",
                analysis = "사주 분석이 필요합니다",
                level = ScoreLevel.AVERAGE
            )
        }

        val nameOhaeng = name.analysisInfo?.ohaengInfo?.baleumOhaeng ?: ""
        val jawonOhaeng = name.analysisInfo?.ohaengInfo?.jawonOhaeng ?: emptyList()
        val missingElements = profileSajuInfo.missingElements

        var score = 70
        var compensationCount = 0
        val compensatedElements = mutableListOf<String>()

        val allNameOhaeng = nameOhaeng.toList().map { it.toString() } + jawonOhaeng

        missingElements.forEach { element ->
            if (allNameOhaeng.contains(element)) {
                score += 10
                compensationCount++
                compensatedElements.add(element)
            }
        }

        val dominantElements = profileSajuInfo.dominantElements
        dominantElements.forEach { element ->
            if (allNameOhaeng.count { it == element } >= 2) {
                score -= 5
            }
        }

        val finalScore = score.coerceIn(0, 100)

        // JSON 데이터에서 설명 가져오기
        val description = when {
            compensationCount >= 2 -> "사주의 부족한 오행을 훌륭히 보완합니다"
            compensationCount == 1 -> "사주의 부족한 오행을 일부 보완합니다"
            missingElements.isEmpty() -> "사주가 균형잡혀 있어 보완이 필요하지 않습니다"
            else -> "사주 보완 효과가 제한적입니다"
        }

        // 부족한 오행에 대한 추천사항 추가 (수정됨)
        val lackingRecommendations = missingElements.flatMap { element ->
            JsonLoader.elementCharacteristics.elementLackingRecommendations[element] ?: emptyList()
        }

        // 오행별 추천 색상 추가
        val recommendedColors = missingElements.flatMap { element ->
            JsonLoader.elementCharacteristics.elementColors[element] ?: emptyList()
        }

        val analysis = buildString {
            append("사주에 부족한 오행: ${missingElements.joinToString(", ").ifEmpty { "없음" }}\n")
            append("이름이 보완하는 오행: ${compensatedElements.joinToString(", ").ifEmpty { "없음" }}\n")
            append("이름의 오행 구성: 발음(${nameOhaeng}), 자원(${jawonOhaeng.joinToString("")})")

            if (recommendedColors.isNotEmpty()) {
                append("\n\n【추천 색상】\n")
                append("부족한 오행을 보완하는 색상: ${recommendedColors.joinToString(", ")}")
            }

            if (lackingRecommendations.isNotEmpty()) {
                append("\n\n【부족한 오행 보완 방법】\n")
                lackingRecommendations.forEach { rec ->
                    append("• $rec\n")
                }
            }
        }

        return ScoreDetail(finalScore, description, analysis.trim(),
            ScoreLevel.values().first { finalScore in it.range })
    }

    /**
     * JSON 데이터를 활용한 음양 균형도 평가 (수정됨 - 100점 만점으로 정규화)
     */
    private fun evaluateYinYangBalanceWithJson(analysisInfo: NameAnalysisInfo): ScoreDetail {
        val eumYangInfo = analysisInfo.eumYangInfo
        val yinyangStrings = JsonLoader.yinyangAnalyzerStrings

        // JSON에서 가져온 점수를 100점 만점으로 변환
        val baseScore = when {
            eumYangInfo.isBalanced && eumYangInfo.balance in 0.4..0.6 ->
                yinyangStrings.balanceScores["perfect"] ?: 95
            eumYangInfo.isBalanced ->
                yinyangStrings.balanceScores["good"] ?: 85
            eumYangInfo.balance in 0.3..0.7 ->
                yinyangStrings.balanceScores["fair"] ?: 70
            eumYangInfo.balance in 0.2..0.8 ->
                yinyangStrings.balanceScores["poor"] ?: 50
            else -> 30
        }

        // 10점 만점을 100점 만점으로 변환 (JSON 데이터가 10점 만점인 경우)
        val score = if (baseScore <= 10) {
            baseScore * 10
        } else {
            baseScore
        }

        val description = when {
            score >= 90 -> yinyangStrings.balancePatterns["balanced"] ?: "완벽한 음양 균형을 이루고 있습니다"
            score >= 70 -> "음양 균형이 양호합니다"
            score >= 50 -> "음양 균형이 보통입니다"
            else -> yinyangStrings.balancePatterns["unbalanced"] ?: "음양이 치우쳐 있습니다"
        }

        // 더 상세한 분석 정보 추가
        val analysis = buildString {
            append(eumYangInfo.balanceDescription).append("\n")
            append("패턴: ${eumYangInfo.pattern}\n")
            append("균형도: ${(eumYangInfo.balance * 100).toInt()}%\n")
            append("음(${eumYangInfo.eumCount}개) : 양(${eumYangInfo.yangCount}개)\n")

            // 음양 패턴에 대한 추가 설명
            when (eumYangInfo.pattern) {
                "음양음", "양음양" -> append("\n조화로운 음양 배치로 균형이 잘 잡혀 있습니다.")
                "음음음" -> append("\n음의 기운이 강하여 내성적이고 신중한 성향을 보입니다.")
                "양양양" -> append("\n양의 기운이 강하여 외향적이고 적극적인 성향을 보입니다.")
                else -> append("\n${eumYangInfo.pattern} 패턴의 독특한 음양 구성을 가지고 있습니다.")
            }
        }

        return ScoreDetail(score, description, analysis,
            ScoreLevel.values().first { score in it.range })
    }

    /**
     * JSON 데이터를 활용한 오행 조화도 평가
     */
    private fun evaluateFiveElementsHarmonyWithJson(analysisInfo: NameAnalysisInfo): ScoreDetail {
        val ohaengInfo = analysisInfo.ohaengInfo
        val elementStrings = JsonLoader.elementAnalyzerStrings

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
                append("\n\n【상생 관계】\n")
                ohaengInfo.generatingPairs.forEach { (from, to) ->
                    val relation = JsonLoader.elementCharacteristics.elementGenerativeRelations["${from}→${to}"]
                    append("• ${from}→${to}: ${relation ?: "상생"}\n")
                }
            }

            if (ohaengInfo.conflictingPairs.isNotEmpty()) {
                append("\n【상극 관계】\n")
                ohaengInfo.conflictingPairs.forEach { (from, to) ->
                    val relation = JsonLoader.elementCharacteristics.elementControllingRelations["${from}→${to}"]
                    append("• ${from}→${to}: ${relation ?: "상극"}\n")
                }
            }

            // 오행별 특성 추가
            val uniqueElements = (ohaengInfo.baleumOhaeng.toSet() + ohaengInfo.jawonOhaeng.toSet())
                .filterNot { it.toString().isEmpty() }
            if (uniqueElements.isNotEmpty()) {
                append("\n【오행별 특성】\n")
                uniqueElements.forEach { element ->
                    val characteristic = JsonLoader.getElementCharacteristic(element.toString())
                    append("• $element: $characteristic\n")
                }

                // 오행별 추천 직업 분야 (수정됨)
                append("\n【오행별 추천 분야】\n")
                uniqueElements.forEach { element ->
                    val careerFields = JsonLoader.elementCharacteristics.elementCareerFields[element.toString()]
                    if (careerFields != null && careerFields.isNotEmpty()) {
                        append("• $element: ${careerFields.take(3).joinToString(", ")}\n")
                    }
                }

                // 오행별 계절/방향 정보 추가
                append("\n【오행별 특성 정보】\n")
                uniqueElements.forEach { element ->
                    val direction = JsonLoader.getElementDirection(element.toString())
                    val season = JsonLoader.getElementSeason(element.toString())
                    val numbers = JsonLoader.getElementNumbers(element.toString())

                    if (direction != null || season != null) {
                        append("• $element: ")
                        direction?.let { append("방향($it) ") }
                        season?.let { append("계절($it) ") }
                        numbers?.let { append("숫자(${it.joinToString(",")})") }
                        append("\n")
                    }
                }
            }
        }

        return ScoreDetail(score, description, analysis.trim(),
            ScoreLevel.values().first { score in it.range })
    }

    /**
     * JSON 데이터를 활용한 획수 길흉 평가
     */
    private fun evaluateStrokeAuspiciousnessWithJson(
        name: GeneratedName,
        analysisInfo: NameAnalysisInfo
    ): ScoreDetail {
        val sagyeok = name.sagyeok
        val sagyeokScore = analysisInfo.scoreBreakdown["사격점수"] ?: 0

        val score = when {
            sagyeokScore >= 100 -> 95
            sagyeokScore >= 75 -> 80
            sagyeokScore >= 50 -> 65
            sagyeokScore >= 25 -> 45
            else -> 25
        }

        val auspiciousCount = sagyeokScore / 25

        // JSON에서 각 획수의 의미 가져오기
        val strokeMeanings = listOf(
            "형격" to JsonLoader.getStrokeMeaning(sagyeok.hyeong),
            "원격" to JsonLoader.getStrokeMeaning(sagyeok.won),
            "이격" to JsonLoader.getStrokeMeaning(sagyeok.i),
            "정격" to JsonLoader.getStrokeMeaning(sagyeok.jeong)
        )

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
            append("길한 사격: ${auspiciousCount}개/4개\n\n")

            // 각 사격의 상세 의미 추가
            strokeMeanings.forEach { (name, meaning) ->
                append("【$name - ${meaning.number}수】 ${meaning.title}\n")
                append("📌 ${meaning.summary}\n")
                append("✨ ${meaning.positiveAspects}\n")

                if (meaning.cautionPoints.isNotEmpty()) {
                    append("⚠️ ${meaning.cautionPoints}\n")
                }

                // 사업운/리더십 정보 추가
                if (JsonLoader.isBusinessLuckStroke(meaning.number)) {
                    append("💼 사업운이 좋은 획수입니다\n")
                }
                if (JsonLoader.isLeadershipStroke(meaning.number)) {
                    append("👑 리더십이 뛰어난 획수입니다\n")
                }

                // 특별한 특성이 있으면 표시
                meaning.specialCharacteristics?.let {
                    append("🌟 특별한 특성: $it\n")
                }

                append("\n")
            }
        }

        return ScoreDetail(score, description, analysis.trim(),
            ScoreLevel.values().first { score in it.range })
    }

    /**
     * 발음 자연스러움 평가
     */
    private fun evaluatePronunciationNaturalnessWithJson(analysisInfo: NameAnalysisInfo): ScoreDetail {
        val pronunciationStep = analysisInfo.filteringSteps
            .find { it.filterName.contains("발음") }

        val score = if (pronunciationStep?.passed == true) {
            85
        } else {
            40
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
     * 성격 분석 (stroke_meanings.json 활용)
     */
    private fun analyzePersonalityWithJson(name: GeneratedName): PersonalityAnalysis {
        val sagyeok = name.sagyeok

        // 각 격의 성격 특성을 가져옴
        val hyeongMeaning = JsonLoader.getStrokeMeaning(sagyeok.hyeong)
        val wonMeaning = JsonLoader.getStrokeMeaning(sagyeok.won)
        val iMeaning = JsonLoader.getStrokeMeaning(sagyeok.i)
        val jeongMeaning = JsonLoader.getStrokeMeaning(sagyeok.jeong)

        // 모든 성격 특성을 수집하고 중복 제거
        val allTraits = mutableListOf<String>()
        allTraits.addAll(hyeongMeaning.personalityTraits)
        allTraits.addAll(wonMeaning.personalityTraits)
        allTraits.addAll(iMeaning.personalityTraits)
        allTraits.addAll(jeongMeaning.personalityTraits)

        // 가장 많이 나타나는 특성을 핵심 특성으로
        val traitFrequency = allTraits.groupingBy { it }.eachCount()
        val coreTraits = traitFrequency.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }

        // 강점과 약점 분석
        val strengths = mutableListOf<String>()
        val weaknesses = mutableListOf<String>()

        // 각 격의 긍정적/주의사항을 강점/약점으로 분류
        listOf(hyeongMeaning, wonMeaning, iMeaning, jeongMeaning).forEach { meaning ->
            if (meaning.positiveAspects.isNotEmpty()) {
                strengths.add(meaning.positiveAspects)
            }
            if (meaning.cautionPoints.isNotEmpty()) {
                weaknesses.add(meaning.cautionPoints)
            }
        }

        // 종합 설명
        val description = buildString {
            append("이 이름의 주인은 ")
            append(coreTraits.take(3).joinToString(", "))
            append("의 특성을 가진 사람입니다. ")

            // 주격(원격)의 특성을 중심으로 설명
            append(wonMeaning.detailedExplanation)
        }

        return PersonalityAnalysis(
            coreTraits = coreTraits,
            strengths = strengths.distinct().take(3),
            weaknesses = weaknesses.distinct().take(3),
            description = description
        )
    }

    /**
     * 적합 직업 분석 (stroke_meanings.json + element_characteristics.json 활용) - 수정됨
     */
    private fun analyzeCareerWithJson(name: GeneratedName, analysisInfo: NameAnalysisInfo): CareerGuidance {
        val sagyeok = name.sagyeok

        // 각 격의 적합 직업을 수집
        val allCareers = mutableListOf<String>()
        listOf(
            JsonLoader.getStrokeMeaning(sagyeok.hyeong),
            JsonLoader.getStrokeMeaning(sagyeok.won),
            JsonLoader.getStrokeMeaning(sagyeok.i),
            JsonLoader.getStrokeMeaning(sagyeok.jeong)
        ).forEach { meaning ->
            allCareers.addAll(meaning.suitableCareer)
        }

        // 오행별 추천 직업 분야 추가 (수정됨)
        val ohaengInfo = analysisInfo.ohaengInfo
        val uniqueElements = (ohaengInfo.baleumOhaeng.toSet() + ohaengInfo.jawonOhaeng.toSet())
            .filterNot { it.toString().isEmpty() }

        val elementCareerFields = uniqueElements.flatMap { element ->
            JsonLoader.elementCharacteristics.elementCareerFields[element.toString()] ?: emptyList()
        }.distinct().take(10)

        // 중복을 제거하고 빈도순으로 정렬
        val careerFrequency = allCareers.groupingBy { it }.eachCount()
        val recommendedCareers = careerFrequency.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }

        // 워크 스타일 분석
        val workStyle = buildString {
            val primaryMeaning = JsonLoader.getStrokeMeaning(sagyeok.won)
            append("주로 ")
            append(primaryMeaning.personalityTraits.take(2).joinToString(", "))
            append("한 스타일로 일합니다. ")

            if (JsonLoader.isBusinessLuckStroke(sagyeok.won)) {
                append("사업가 기질이 있어 독립적인 사업이 유리합니다. ")
            }
            if (JsonLoader.isLeadershipStroke(sagyeok.hyeong)) {
                append("리더십이 뛰어나 관리직이나 임원직에 적합합니다.")
            }
        }

        return CareerGuidance(
            recommendedCareers = recommendedCareers,
            careerFields = elementCareerFields,
            workStyle = workStyle,
            successFactors = listOf(
                "타고난 재능을 살린 분야 선택",
                "성격 특성에 맞는 업무 환경",
                "오행 조화를 고려한 직업 선택"
            )
        )
    }

    /**
     * 인생 시기별 분석 (stroke_meanings.json 활용)
     */
    private fun analyzeLifePeriodsWithJson(name: GeneratedName): LifePeriodAnalysis {
        val sagyeok = name.sagyeok
        val meanings = mapOf(
            "유년기(1-20세)" to JsonLoader.getStrokeMeaning(sagyeok.hyeong),
            "청년기(21-40세)" to JsonLoader.getStrokeMeaning(sagyeok.won),
            "중년기(41-60세)" to JsonLoader.getStrokeMeaning(sagyeok.i),
            "노년기(61세 이후)" to JsonLoader.getStrokeMeaning(sagyeok.jeong)
        )

        val periods = meanings.map { (periodName, meaning) ->
            val challenges = meaning.challengePeriod?.let { listOf(it) } ?: emptyList()
            val opportunities = meaning.opportunityArea?.let { listOf(it) } ?: emptyList()

            LifePeriod(
                name = periodName,
                description = meaning.lifePeriodInfluence,
                challenges = challenges,
                opportunities = opportunities,
                advice = buildString {
                    append("이 시기는 ${meaning.title}의 영향을 받습니다. ")
                    append(meaning.summary)
                }
            )
        }

        // 전체 인생 흐름 분석
        val overallFlow = buildString {
            append("전반적으로 ")

            // 형격과 원격이 좋으면 초중년 성공
            val hyeong = sagyeok.hyeong
            val won = sagyeok.won
            if (JsonLoader.getStrokeMeaning(hyeong).number in listOf(1, 3, 5, 6, 7, 8, 11, 13, 15, 16) &&
                JsonLoader.getStrokeMeaning(won).number in listOf(1, 3, 5, 6, 7, 8, 11, 13, 15, 16)) {
                append("초년부터 중년까지 순조로운 발전이 예상됩니다. ")
            }

            // 이격과 정격이 좋으면 중노년 안정
            val i = sagyeok.i
            val jeong = sagyeok.jeong
            if (JsonLoader.getStrokeMeaning(i).number in listOf(5, 6, 15, 16, 24, 31, 32, 35) &&
                JsonLoader.getStrokeMeaning(jeong).number in listOf(5, 6, 15, 16, 24, 31, 32, 35)) {
                append("중년 이후 안정적인 삶이 예상됩니다.")
            }
        }

        return LifePeriodAnalysis(
            periods = periods,
            overallFlow = overallFlow,
            criticalAges = extractCriticalAges(meanings.values.toList())
        )
    }

    private fun extractCriticalAges(meanings: List<com.ssc.namespring.model.data.json.StrokeMeaningDetail>): List<Int> {
        val criticalAges = mutableListOf<Int>()

        meanings.forEach { meaning ->
            // challengePeriod에서 나이 추출 (예: "20-30대에 시련이 많음" -> [20, 30])
            meaning.challengePeriod?.let { period ->
                val agePattern = Regex("(\\d+)")
                agePattern.findAll(period).forEach { match ->
                    criticalAges.add(match.groupValues[1].toInt())
                }
            }
        }

        return criticalAges.distinct().sorted()
    }

    /**
     * JSON 데이터를 활용한 추천사항 추출
     */
    private fun extractRecommendationsWithJson(
        analysisInfo: NameAnalysisInfo,
        overallScore: Int,
        vararg details: ScoreDetail
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // 기존 추천사항
        recommendations.addAll(analysisInfo.recommendations)

        // 점수별 추천사항 추가
        val scoreEvals = JsonLoader.scoreEvaluations
        val recommendation = when {
            overallScore >= 80 -> scoreEvals.recommendations["80_above"]
            overallScore >= 70 -> scoreEvals.recommendations["70_above"]
            overallScore >= 60 -> scoreEvals.recommendations["60_above"]
            else -> scoreEvals.recommendations["60_below"]
        }
        recommendation?.let { recommendations.add(it) }

        // 높은 점수 항목들의 설명 추가
        details.forEach { detail ->
            if (detail.level == ScoreLevel.EXCELLENT || detail.level == ScoreLevel.GOOD) {
                recommendations.add(detail.description)
            }
        }

        // 상세 추천사항 추가
        if (details.any { it.level == ScoreLevel.EXCELLENT }) {
            scoreEvals.detailedRecommendations.values.take(2).forEach {
                recommendations.add(it)
            }
        }

        return recommendations.distinct()
    }

    /**
     * JSON 데이터를 활용한 개선사항 추출
     */
    private fun extractImprovementsWithJson(
        overallScore: Int,
        vararg details: ScoreDetail
    ): List<String> {
        val improvements = mutableListOf<String>()

        // 낮은 점수 항목들의 개선사항
        details.forEach { detail ->
            if (detail.level == ScoreLevel.BELOW || detail.level == ScoreLevel.POOR) {
                improvements.add(detail.analysis)
            }
        }

        // 점수가 낮은 경우 일반적인 개선사항 추가
        if (overallScore < 60) {
            val scoreEvals = JsonLoader.scoreEvaluations
            scoreEvals.weaknessMessages.values.forEach { weakness ->
                improvements.add(weakness)
            }
        }

        return improvements.distinct()
    }

    /**
     * 이름봄 점수 계산
     */
    private fun calculateNamebomScore(analysisInfo: NameAnalysisInfo): Int {
        val scoreBreakdown = analysisInfo.scoreBreakdown

        val normalizedScores = mapOf(
            "사격점수" to (scoreBreakdown["사격점수"] ?: 0) * 1.0,
            "음양균형" to (scoreBreakdown["음양균형"] ?: 0) * 5.0,
            "오행조화" to (scoreBreakdown["오행조화"] ?: 0) * 5.0,
            "획수길흉" to (scoreBreakdown["획수길흉"] ?: 0) * 5.0
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

    suspend fun generateComparisonReport(
        names: List<GeneratedName>,
        profile: Profile
    ): Result<ComparisonReport> {
        return try {
            require(names.size in 2..4) { "비교는 2~4개의 이름만 가능합니다" }

            val comparisonResults = mutableMapOf<String, List<ComparisonScore>>()
            val categories = listOf("사주보완", "음양균형", "오행조화", "획수길흉", "발음자연", "종합점수")

            categories.forEach { category ->
                val scores = names.map { name ->
                    val score = when (category) {
                        "사주보완" -> evaluateSajuCompensationWithJson(name, profile).score
                        "음양균형" -> name.analysisInfo?.let { evaluateYinYangBalanceWithJson(it).score } ?: 0
                        "오행조화" -> name.analysisInfo?.let { evaluateFiveElementsHarmonyWithJson(it).score } ?: 0
                        "획수길흉" -> name.analysisInfo?.let { evaluateStrokeAuspiciousnessWithJson(name, it).score } ?: 0
                        "발음자연" -> name.analysisInfo?.let { evaluatePronunciationNaturalnessWithJson(it).score } ?: 0
                        "종합점수" -> name.analysisInfo?.let { calculateNamebomScore(it) } ?: 0
                        else -> 0
                    }
                    ComparisonScore(name, score, 0)
                }.sortedByDescending { it.score }.mapIndexed { index, score ->
                    score.copy(rank = index + 1)
                }

                comparisonResults[category] = scores
            }

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

    suspend fun getEvaluationReport(reportId: String): EvaluationReport? {
        return try {
            repository.getEvaluationReport(reportId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getComparisonReport(reportId: String): ComparisonReport? {
        return try {
            repository.getComparisonReport(reportId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getRecentReports(limit: Int = 10): List<Any> {
        return try {
            repository.getRecentReports(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getEvaluationReportsByProfile(profileId: String): List<EvaluationReport> {
        return try {
            repository.getEvaluationReportsByProfile(profileId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getComparisonReportsByProfile(profileId: String): List<ComparisonReport> {
        return try {
            repository.getComparisonReportsByProfile(profileId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteEvaluationReport(reportId: String) {
        repository.deleteEvaluationReport(reportId)
    }

    suspend fun deleteComparisonReport(reportId: String) {
        repository.deleteComparisonReport(reportId)
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