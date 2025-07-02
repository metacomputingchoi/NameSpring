// utils/LoggingHelper.kt
package com.ssc.namespring.utils

import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.data.EvaluationReport
import java.time.LocalDateTime

/**
 * 향상된 로깅을 위한 헬퍼 클래스
 * JSON 데이터를 활용하여 더 의미있고 도움이 되는 로깅 제공
 */
object LoggingHelper {

    private val logger = AndroidLogger("LoggingHelper")

    /**
     * 이름 생성 결과 로깅
     */
    fun logNameGenerationResult(names: List<GeneratedName>, profile: Profile, elapsedTime: Long) {
        val topScore = names.maxOfOrNull { UiHelper.getNamebomScore(it) } ?: 0
        val avgScore = names.map { UiHelper.getNamebomScore(it) }.average().toInt()

        logger.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.d("📊 작명 결과 통계")
        logger.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.d("프로필: ${profile.profileName} (${profile.getFullName()})")
        logger.d("생성 시간: ${elapsedTime}ms")
        logger.d("생성 개수: ${names.size}개")
        logger.d("최고 점수: ${topScore}점 ${UiHelper.getSproutIcon(topScore)}")
        logger.d("평균 점수: ${avgScore}점")
        logger.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // 점수대별 분포
        val distribution = names.groupBy { score ->
            when (val s = UiHelper.getNamebomScore(score)) {
                in 90..100 -> "90-100점"
                in 80..89 -> "80-89점"
                in 70..79 -> "70-79점"
                in 60..69 -> "60-69점"
                else -> "60점 미만"
            }
        }

        logger.d("점수 분포:")
        distribution.forEach { (range, list) ->
            val bar = UiHelper.createMiniProgressBar(list.size, names.size)
            logger.d("  $range: $bar ${list.size}개")
        }

        // 사주 보완 통계
        profile.sajuInfo?.missingElements?.let { missingElements ->
            val compensatedCount = names.count { name ->
                name.analysisInfo?.sajuInfo?.missingElements?.any { it in missingElements } == true
            }
            logger.d("")
            logger.d("사주 보완율: ${(compensatedCount * 100 / names.size)}%")
        }
    }

    /**
     * 평가 결과 상세 로깅
     */
    fun logEvaluationResult(report: EvaluationReport) {
        val grade = JsonLoader.getGrade(report.overallScore)
        val scoreMessage = JsonLoader.getScoreRangeMessage(report.overallScore)

        logger.d("╔═══════════════════════════════════════════╗")
        logger.d("║           이름 평가 결과 보고서           ║")
        logger.d("╚═══════════════════════════════════════════╝")
        logger.d("")
        logger.d("이름: ${report.getDisplayName()} (${report.getDisplayHanja()})")
        logger.d("종합: ${report.overallScore}점 [${grade}등급] ${scoreMessage?.emoji ?: ""}")
        logger.d("")

        // 레이더 차트 시각화
        logger.d("【평가 항목별 분석】")
        val categories = listOf(
            "사주보완" to report.sajuCompensation.score,
            "음양균형" to report.yinYangBalance.score,
            "오행조화" to report.fiveElementsHarmony.score,
            "획수길흉" to report.strokeAuspiciousness.score,
            "발음자연" to report.pronunciationNaturalness.score
        )

        categories.forEach { (name, score) ->
            val bar = UiHelper.createMiniProgressBar(score)
            val excellence = JsonLoader.getCategoryExcellenceMessage(name, score)
            logger.d("$name: $bar ${score}점")
            if (excellence.isNotEmpty()) {
                logger.d("       → $excellence")
            }
        }

        // 특별한 조합 체크
        checkAndLogSpecialCombinations(report)
    }

    /**
     * 특별한 조합 로깅
     */
    private fun checkAndLogSpecialCombinations(report: EvaluationReport) {
        val combinations = mutableListOf<String>()

        if (report.sajuCompensation.score >= 90 && report.fiveElementsHarmony.score >= 90) {
            JsonLoader.getSpecialCombinationMessage("perfect_saju_ohaeng")?.let {
                combinations.add("🌟 $it")
            }
        }

        if (report.yinYangBalance.score >= 95) {
            JsonLoader.getSpecialCombinationMessage("perfect_yin_yang")?.let {
                combinations.add("☯️ $it")
            }
        }

        if (report.strokeAuspiciousness.score >= 90) {
            JsonLoader.getSpecialCombinationMessage("all_good_strokes")?.let {
                combinations.add("🎯 $it")
            }
        }

        if (combinations.isNotEmpty()) {
            logger.d("")
            logger.d("【특별 분석】")
            combinations.forEach { logger.d(it) }
        }
    }

    /**
     * 시작 화면 로깅
     */
    fun logAppStart() {
        val hour = LocalDateTime.now().hour
        val month = LocalDateTime.now().monthValue

        val timeMessage = JsonLoader.getTimeBasedMessage(hour)
        val seasonalMessage = JsonLoader.getSeasonalMessage(month)

        logger.d("╔═══════════════════════════════════════════╗")
        logger.d("║          🌸 이름봄 앱 시작 🌸            ║")
        logger.d("╚═══════════════════════════════════════════╝")

        timeMessage?.let { logger.d("⏰ $it") }
        seasonalMessage?.let {
            logger.d("🌿 ${it.message}")
            logger.d("   행운 오행: ${it.luckyElements.joinToString(", ")}")
        }

        logger.d("")
        logger.d("앱 버전: 1.0.0")
        logger.d("작명 엔진: NamingEngine v2.0")
        logger.d("JSON 데이터: 최신 버전 로드 완료")
    }

    /**
     * 작명 팁 로깅
     */
    fun logNamingTips(profile: Profile) {
        logger.d("💡 ${profile.profileName}님을 위한 맞춤 작명 팁")
        logger.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // 사주 기반 팁
        profile.sajuInfo?.missingElements?.forEach { element ->
            JsonLoader.getSajuBasedTips(element)?.let { tips ->
                logger.d("")
                logger.d("【${tips.description}】")
                tips.tips.take(2).forEach { tip ->
                    logger.d("• $tip")
                }
                if (tips.recommendedHanja.isNotEmpty()) {
                    logger.d("추천 한자: ${tips.recommendedHanja.take(5).joinToString(", ")}")
                }
            }
        }

        // 현대 작명 트렌드
        val modernTrends = JsonLoader.getModernNamingTrends()
        logger.d("")
        logger.d("【${modernTrends.title}】")
        modernTrends.tips.take(3).forEach { tip ->
            logger.d("• $tip")
        }
    }

    /**
     * 프로필 생성 축하 로깅
     */
    fun logProfileCreation(profile: Profile) {
        logger.d("")
        logger.d("🎉 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 🎉")
        logger.d("   축하합니다! 프로필이 생성되었습니다!")
        logger.d("🎉 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 🎉")
        logger.d("")
        logger.d("프로필명: ${profile.profileName}")
        logger.d("이름: ${profile.getFullName()} (${profile.getFullHanjaName()})")
        logger.d("생년월일시: ${UiHelper.formatDateTime(profile.birthDateTime)}")

        profile.sajuInfo?.let { saju ->
            logger.d("")
            logger.d("【사주 분석 요약】")
            logger.d("사주팔자: ${saju.fourPillars.joinToString(" ")}")
            logger.d(saju.getElementDescription())

            if (saju.missingElements.isNotEmpty()) {
                logger.d("")
                logger.d("💡 작명 시 ${saju.missingElements.joinToString(", ")} 오행을 보완하면 좋습니다")
            }
        }

        logger.d("")
        logger.d("🌱 이제 이름봄과 함께 최고의 이름을 찾아보세요!")
    }
}