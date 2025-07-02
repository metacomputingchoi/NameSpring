// controller/EvaluationController.kt
package com.ssc.namespring.controller

import com.ssc.namingengine.NamingEngine
import com.ssc.namespring.model.ReportModel
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.utils.JsonLoader
import com.ssc.namespring.utils.LoggingHelper
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.utils.PdfExportUtil
import com.ssc.namespring.view.EvaluationInputView
import com.ssc.namespring.view.EvaluationResultView
import com.ssc.namespring.view.impl.EvaluationInputViewImpl
import kotlinx.coroutines.*

class EvaluationController(
    private val namingEngine: NamingEngine,
    private val reportModel: ReportModel,
    private val evaluationInputView: EvaluationInputView,
    private val evaluationResultView: EvaluationResultView
) {

    private val logger = AndroidLogger("EvaluationController")

    // 컨트롤러 전용 코루틴 스코프 추가
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // 평가 완료 콜백
    var onEvaluationCompleted: (() -> Unit)? = null

    // 평가 횟수 (마일스톤 체크용)
    private var evaluationCount = 0

    suspend fun showEvaluationInput(profiles: List<Profile>) {
        evaluationInputView.showDynamicEvaluationInput()
        evaluationInputView.showProfileSelector(profiles)

        // 평가 기능 가이드
        JsonLoader.getFeatureGuide("evaluation")?.let { guide ->
            logger.d("")
            logger.d("【${guide.title}】")
            logger.d(guide.description)
            logger.d("")
            logger.d("💡 도움말:")
            guide.tips.forEach { tip ->
                logger.d("• $tip")
            }
        }

        // 테스트용 입력 설정
        if (profiles.isNotEmpty()) {
            (evaluationInputView as? EvaluationInputViewImpl)?.setTestInput(
                name = "민준",
                hanja = "民俊",
                profile = profiles.first()
            )

            // 자동으로 평가 실행 (테스트)
            handleEvaluationInput()
        }
    }

    suspend fun handleEvaluationInput() {
        if (!evaluationInputView.validateInput()) {
            return
        }

        val profile = evaluationInputView.getSelectedProfile() ?: return
        val nameHangul = evaluationInputView.getEvaluationName()
        val nameHanja = evaluationInputView.getEvaluationHanja()

        // 평가 카운트 증가
        evaluationCount++

        try {
            // 이름 입력 형식 생성
            val nameInput = buildNameInput(
                profile.surname,
                profile.surnameHanja,
                nameHangul,
                nameHanja
            )

            logger.d("평가 입력: $nameInput")

            // 평가 실행
            val evaluatedNames = withContext(Dispatchers.IO) {
                namingEngine.generateNames(
                    userInput = nameInput,
                    birthDateTime = profile.birthDateTime,
                    useYajasi = profile.useYajasi,
                    verbose = false,
                    withoutFilter = true
                )
            }

            val evaluatedName = evaluatedNames.firstOrNull()
            if (evaluatedName == null) {
                evaluationInputView.showError("이름 평가 실패")
                showEvaluationErrorGuidance()
                return
            }

            // 평가 보고서 생성
            val report = reportModel.generateEvaluationReport(
                evaluatedName,
                profile
            ).getOrThrow()

            // 상세한 평가 결과 로깅
            LoggingHelper.logEvaluationResult(report)

            // 마일스톤 체크
            checkEvaluationMilestones(report)

            // 결과 표시
            handleReportGeneration(report)

        } catch (e: Exception) {
            evaluationInputView.showError("평가 실패: ${e.message}")
            showEvaluationErrorGuidance()
        }
    }

    /**
     * 평가 오류 시 안내
     */
    private fun showEvaluationErrorGuidance() {
        logger.d("")
        logger.d("💡 평가 오류 해결 방법:")

        JsonLoader.getErrorMessage("input_errors", "invalid_hangul")?.let {
            logger.d("• $it")
        }
        JsonLoader.getErrorMessage("input_errors", "invalid_hanja")?.let {
            logger.d("• $it")
        }

        logger.d("• 한글과 한자가 서로 대응되는지 확인하세요")
        logger.d("• 성씨와 이름의 글자 수가 맞는지 확인하세요")
    }

    /**
     * 평가 마일스톤 체크
     */
    private fun checkEvaluationMilestones(report: com.ssc.namespring.model.data.EvaluationReport) {
        // 첫 평가
        if (evaluationCount == 1) {
            JsonLoader.getMilestoneMessage("first_evaluation")?.let { message ->
                logger.d("")
                logger.d("🎯 $message")
            }
        }

        // 90점 이상 첫 발견
        if (report.overallScore >= 90) {
            JsonLoader.getMilestoneMessage("first_90_score")?.let { message ->
                logger.d("")
                logger.d("🏆 $message")
            }
        }

        // 완벽한 조합 발견
        if (report.sajuCompensation.score >= 90 &&
            report.yinYangBalance.score >= 90 &&
            report.fiveElementsHarmony.score >= 90) {
            logger.d("")
            logger.d("🌟 완벽한 조합을 발견하셨습니다!")
        }
    }

    fun handleReportGeneration(report: com.ssc.namespring.model.data.EvaluationReport) {
        // 종합 점수 표시
        evaluationResultView.showOverallScore(report.overallScore)

        // 점수별 테마 적용
        evaluationResultView.applyScoreTheme(report.overallScore)

        // 레이더 차트 데이터
        evaluationResultView.showRadarChart(report.getRadarChartData())

        // 상세 분석
        evaluationResultView.showDetailedAnalysis(report)

        // 점수대별 맞춤 추천사항
        showCustomizedRecommendations(report)

        // 추천사항 및 개선사항
        evaluationResultView.showRecommendations(report.recommendations)
        evaluationResultView.showImprovements(report.improvements)

        // 저장/공유 옵션
        evaluationResultView.showSaveOptions()
        evaluationResultView.showShareOptions()

        // 평가 완료 콜백 호출
        controllerScope.launch {
            delay(2000)
            onEvaluationCompleted?.invoke()
        }
    }

    /**
     * 점수대별 맞춤 추천사항 표시
     */
    private fun showCustomizedRecommendations(report: com.ssc.namespring.model.data.EvaluationReport) {
        val scoreMessage = JsonLoader.getScoreRangeMessage(report.overallScore)

        if (scoreMessage != null && scoreMessage.recommendations.isNotEmpty()) {
            logger.d("")
            logger.d("【${scoreMessage.title} - 맞춤 조언】")
            scoreMessage.recommendations.forEach { rec ->
                logger.d("💡 $rec")
            }
        }

        // 강점과 약점에 대한 상세 메시지
        val scoreEvals = JsonLoader.scoreEvaluations

        if (report.sajuCompensation.score >= 80) {
            scoreEvals.strengthMessages["saju_complement"]?.let {
                logger.d("✨ $it")
            }
        }

        if (report.yinYangBalance.score < 60) {
            scoreEvals.weaknessMessages["yin_yang_balance"]?.let {
                logger.d("⚠️ $it")
            }
        }

        // 상세 추천사항
        when {
            report.overallScore >= 80 -> {
                scoreEvals.detailedRecommendations.values.take(2).forEach {
                    logger.d("• $it")
                }
            }
            report.overallScore >= 60 -> {
                scoreEvals.detailedRecommendations["low_element_harmony"]?.let {
                    logger.d("• $it")
                }
            }
        }
    }

    suspend fun handleReportExport(format: ExportFormat) {
        when (format) {
            ExportFormat.PDF -> {
                logger.d("PDF로 저장 중...")
                // PDF 생성은 MainActivity에서 context를 받아 처리
            }
            ExportFormat.IMAGE -> {
                logger.d("이미지로 저장 중...")
                // 이미지 생성 로직
            }
            ExportFormat.TEXT -> {
                logger.d("텍스트로 저장 중...")
                // 텍스트 생성 로직
            }
        }
    }

    private fun buildNameInput(
        surname: String,
        surnameHanja: String,
        givenName: String,
        givenNameHanja: String
    ): String {
        val surnameInput = surname.mapIndexed { index, char ->
            val hanja = surnameHanja.getOrNull(index) ?: "_"
            "[$char/$hanja]"
        }.joinToString("")

        val givenNameInput = givenName.mapIndexed { index, char ->
            val hanja = givenNameHanja.getOrNull(index) ?: "_"
            "[$char/$hanja]"
        }.joinToString("")

        return surnameInput + givenNameInput
    }

    enum class ExportFormat {
        PDF, IMAGE, TEXT
    }
}