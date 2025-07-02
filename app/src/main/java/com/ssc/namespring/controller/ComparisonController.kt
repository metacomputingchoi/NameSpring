// controller/ComparisonController.kt
package com.ssc.namespring.controller

import com.ssc.namingengine.data.GeneratedName
import com.ssc.namingengine.data.HanjaInfo
import com.ssc.namingengine.data.Sagyeok
import com.ssc.namingengine.data.analysis.*
import com.ssc.namespring.model.ReportModel
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.utils.JsonLoader
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.ComparisonView
import com.ssc.namespring.view.utils.ViewLogger
import com.ssc.namingengine.data.analysis.component.EumYangAnalysisInfo
import com.ssc.namingengine.data.analysis.component.OhaengAnalysisInfo
import com.ssc.namingengine.data.analysis.component.SajuAnalysisInfo
import kotlinx.coroutines.*

class ComparisonController(
    private val reportModel: ReportModel,
    private val comparisonView: ComparisonView
) {

    private val logger = AndroidLogger("ComparisonController")

    // 컨트롤러 전용 코루틴 스코프
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val selectedNames = mutableListOf<GeneratedName>()
    private var selectedProfile: Profile? = null
    private var comparisonCount = 0

    // 비교 완료 콜백
    var onComparisonCompleted: (() -> Unit)? = null

    suspend fun showComparison(profiles: List<Profile>) {
        comparisonView.showNameSelectors(2) // 기본 2개 비교
        comparisonView.showProfileSelector(profiles)

        // 비교 기능 가이드
        JsonLoader.getFeatureGuide("comparison")?.let { guide ->
            ViewLogger.logSection(guide.title)
            logger.d(guide.description)
            ViewLogger.logTipBox(guide.tips)
        }

        // 테스트용 자동 실행
        if (profiles.isNotEmpty()) {
            selectedProfile = profiles.first()
            simulateNameSelection()
        }
    }

    suspend fun handleNameSelection(names: List<GeneratedName>) {
        selectedNames.clear()
        selectedNames.addAll(names)

        if (selectedNames.size >= 2) {
            comparisonView.enableCompareButton(true)
        }
    }

    suspend fun handleComparison() {
        val profile = selectedProfile ?: return

        if (selectedNames.size < 2) {
            comparisonView.showError("2개 이상의 이름을 선택하세요")
            return
        }

        comparisonCount++

        comparisonView.showLoading(true)

        try {
            val report = reportModel.generateComparisonReport(
                names = selectedNames,
                profile = profile
            ).getOrThrow()

            // 비교 결과 표시
            handleRankingDisplay(report)

            // 마일스톤 체크
            checkComparisonMilestones()

        } catch (e: Exception) {
            comparisonView.showError("비교 실패: ${e.message}")
            showComparisonErrorGuidance()
        } finally {
            comparisonView.showLoading(false)
        }
    }

    /**
     * 비교 마일스톤 체크
     */
    private fun checkComparisonMilestones() {
        // 첫 비교
        if (comparisonCount == 1) {
            JsonLoader.getMilestoneMessage("first_comparison")?.let { message ->
                logger.d("")
                logger.d("🎯 $message")
            }
        }

        // 많은 비교 수행
        if (comparisonCount >= 10) {
            val encouragements = JsonLoader.getEncouragementMessage("many_attempts")
            if (encouragements.isNotEmpty()) {
                logger.d("")
                logger.d("👏 ${encouragements.random()}")
            }
        }
    }

    /**
     * 비교 오류 안내
     */
    private fun showComparisonErrorGuidance() {
        logger.d("")
        logger.d("💡 비교 오류 해결 방법:")
        logger.d("• 이름을 2개 이상 선택했는지 확인하세요")
        logger.d("• 프로필이 올바르게 선택되었는지 확인하세요")

        JsonLoader.getErrorMessage("system_errors", "comparison_failed")?.let {
            logger.d("• $it")
        }
    }

    fun handleRankingDisplay(report: com.ssc.namespring.model.data.ComparisonReport) {
        // 비교표 표시
        comparisonView.showComparisonTable(report)

        // 순위 표시
        comparisonView.showRankings(report.rankings)

        // 카테고리별 우수 이름 분석
        showCategoryWinners(report)

        // 최종 추천
        comparisonView.showWinner(report.winnerName)

        // 특별한 조합 체크
        checkSpecialCombinations(report)

        // 의미 조화 분석
        analyzeMeaningHarmony(report)

        // 비교 완료 콜백 호출
        controllerScope.launch {
            delay(2000)
            onComparisonCompleted?.invoke()
        }
    }

    /**
     * 카테고리별 우수 이름 분석
     */
    private fun showCategoryWinners(report: com.ssc.namespring.model.data.ComparisonReport) {
        ViewLogger.logSection("카테고리별 1위", ViewLogger.LineStyle.THIN)

        report.comparisonResults.forEach { (category, scores) ->
            val winner = scores.firstOrNull { it.rank == 1 }
            if (winner != null) {
                val excellence = JsonLoader.getCategoryExcellenceMessage(category, winner.score)
                logger.d("${category}: ${winner.name.surnameHangul}${winner.name.combinedPronounciation}")
                logger.d("   → $excellence")
            }
        }
    }

    /**
     * 특별한 조합 체크
     */
    private fun checkSpecialCombinations(report: com.ssc.namespring.model.data.ComparisonReport) {
        val specialCombinations = mutableListOf<String>()

        report.rankings.forEach { ranking ->
            val name = ranking.name
            val analysisInfo = name.analysisInfo ?: return@forEach

            // 완벽한 조합들 체크
            if (analysisInfo.eumYangInfo.isBalanced &&
                analysisInfo.ohaengInfo.overallHarmony.contains("조화") &&
                (analysisInfo.scoreBreakdown["사격점수"] ?: 0) >= 75) {
                specialCombinations.add("${ranking.getDisplayName()}: 음양오행이 완벽히 조화된 이름")
            }
        }

        if (specialCombinations.isNotEmpty()) {
            ViewLogger.logSection("특별 분석", ViewLogger.LineStyle.DOTTED)
            specialCombinations.forEach { logger.d("✨ $it") }
        }
    }

    /**
     * 의미 조화 분석
     */
    private fun analyzeMeaningHarmony(report: com.ssc.namespring.model.data.ComparisonReport) {
        val meaningTips = JsonLoader.getMeaningCombinationTips()

        ViewLogger.logSection("한자 의미 분석", ViewLogger.LineStyle.THIN)

        report.comparedNames.forEach { name ->
            val meanings = name.hanjaDetails.map { it.inmyongMeaning }
            logger.d("")
            logger.d("${name.surnameHangul}${name.combinedPronounciation}: ${meanings.joinToString(" + ")}")

            // 좋은 조합인지 체크
            if (meanings.size >= 2) {
                val isGoodCombination = meaningTips.goodCombinations.any { combination ->
                    meanings.any { it.contains(combination.substringBefore(" + ")) } &&
                            meanings.any { it.contains(combination.substringAfter(" + ")) }
                }

                if (isGoodCombination) {
                    logger.d("   💫 의미가 조화롭게 어우러집니다")
                }
            }
        }
    }

    // 테스트용 이름 선택 시뮬레이션 (개선됨)
    private suspend fun simulateNameSelection() {
        logger.d("테스트용 이름 생성 중...")

        // 실제 GeneratedName 구조에 맞는 테스트 데이터 생성
        val testNames = listOf(
            createTestName("민준", "民俊", listOf(
                HanjaInfo("民", "백성", "민", "陰", "陰", "水", "土", 5, 5),
                HanjaInfo("俊", "뛰어날", "준", "陰", "陽", "水", "木", 9, 9)
            ), 82, mapOf("사격점수" to 75, "음양균형" to 16, "오행조화" to 15, "획수길흉" to 15)),

            createTestName("서준", "瑞俊", listOf(
                HanjaInfo("瑞", "상서로울", "서", "陰", "陰", "木", "金", 14, 13),
                HanjaInfo("俊", "뛰어날", "준", "陰", "陽", "水", "木", 9, 9)
            ), 89, mapOf("사격점수" to 100, "음양균형" to 10, "오행조화" to 20, "획수길흉" to 20)),

            createTestName("도윤", "道潤", listOf(
                HanjaInfo("道", "길", "도", "陽", "陰", "火", "土", 13, 12),
                HanjaInfo("潤", "윤택할", "윤", "陰", "陰", "水", "水", 16, 15)
            ), 73, mapOf("사격점수" to 50, "음양균형" to 16, "오행조화" to 15, "획수길흉" to 10))
        )

        handleNameSelection(testNames)
        handleComparison()
    }

    private fun createTestName(
        hangul: String,
        hanja: String,
        hanjaInfoList: List<HanjaInfo>,
        totalScore: Int,
        scoreBreakdown: Map<String, Int>
    ): GeneratedName {
        val profile = selectedProfile ?: Profile(
            id = "test",
            profileName = "테스트",
            surname = "김",
            surnameHanja = "金",
            givenName = "테스트",
            givenNameHanja = "測試",
            birthDateTime = java.time.LocalDateTime.now(),
            useYajasi = false
        )

        // 사격 계산 (간단한 예시)
        val sagyeok = Sagyeok(
            hyeong = 15 + hanjaInfoList[0].wonHoeksu,
            won = hanjaInfoList.sumOf { it.wonHoeksu },
            i = hanjaInfoList[1].wonHoeksu + 1,
            jeong = 15 + hanjaInfoList.sumOf { it.wonHoeksu }
        )

        // 사주 분석 정보 (테스트용)
        val sajuInfo = SajuAnalysisInfo(
            fourPillars = arrayOf("甲子", "乙丑", "丙寅", "丁卯"),
            sajuOhaengCount = mapOf("木" to 2, "火" to 2, "土" to 1, "金" to 1, "水" to 2),
            missingElements = listOf("土"),
            dominantElements = listOf("木", "火"),
            elementBalance = mapOf("木" to 0.25f, "火" to 0.25f, "土" to 0.125f, "金" to 0.125f, "水" to 0.25f)
        )

        // 음양 분석 정보
        val eumYangInfo = EumYangAnalysisInfo(
            combinedEumyang = if (hanjaInfoList.count { it.baleumEumyang == "陰" } > hanjaInfoList.count { it.baleumEumyang == "陽" }) "음음음" else "음양음",
            eumCount = hanjaInfoList.count { it.baleumEumyang == "陰" },
            yangCount = hanjaInfoList.count { it.baleumEumyang == "陽" },
            balance = if (hanjaInfoList.count { it.baleumEumyang == "陰" } == hanjaInfoList.count { it.baleumEumyang == "陽" }) 0.5f else 0.67f,
            pattern = if (hanjaInfoList.count { it.baleumEumyang == "陰" } == hanjaInfoList.count { it.baleumEumyang == "陽" }) "음양 균형" else "음 우세",
            isBalanced = hanjaInfoList.count { it.baleumEumyang == "陰" } in 1..2,
            balanceDescription = "음양 균형이 적절합니다"
        )

        // 오행 분석 정보
        val ohaengInfo = OhaengAnalysisInfo(
            baleumOhaeng = hanjaInfoList.map { it.baleumOhaeng }.joinToString(""),
            hoeksuOhaeng = listOf(8, 8, 0),
            jawonOhaeng = hanjaInfoList.map { it.jawonOhaeng },
            sagyeokSuriOhaeng = listOf(6, 8, 8, 4),
            harmonyScore = scoreBreakdown["오행조화"] ?: 15,
            conflictingPairs = emptyList(),
            generatingPairs = listOf("木" to "火"),
            overallHarmony = if ((scoreBreakdown["오행조화"] ?: 0) >= 15) "조화로움" else "보통"
        )

        // 필터링 정보
        val filteringSteps = listOf(
            FilteringStep("발음오행음양필터", true, "필터 통과", emptyMap()),
            FilteringStep("자원오행필터", true, "필터 통과", emptyMap()),
            FilteringStep("발음자연스러움필터", true, "필터 통과", emptyMap())
        )

        // 분석 정보 생성
        val analysisInfo = NameAnalysisInfo(
            sajuInfo = sajuInfo,
            eumYangInfo = eumYangInfo,
            ohaengInfo = ohaengInfo,
            filteringSteps = filteringSteps,
            totalScore = totalScore,
            scoreBreakdown = scoreBreakdown,
            recommendations = listOf(
                "음양이 조화롭게 균형을 이루고 있습니다.",
                "오행이 서로 상생하여 조화롭습니다.",
                "이름의 뜻: ${hanjaInfoList.joinToString(" + ") { it.inmyongMeaning }}"
            )
        )

        return GeneratedName(
            surnameHangul = profile.surname,
            surnameHanja = profile.surnameHanja,
            combinedHanja = hanja,
            combinedPronounciation = hangul,
            sagyeok = sagyeok,
            nameHanjaHoeksu = hanjaInfoList.map { it.wonHoeksu },
            hanjaDetails = hanjaInfoList,
            analysisInfo = analysisInfo
        )
    }
}