// controller/EvaluationController.kt
package com.ssc.namespring.controller

import com.ssc.namingengine.NamingEngine
import com.ssc.namespring.model.ReportModel
import com.ssc.namespring.model.data.Profile
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

    suspend fun showEvaluationInput(profiles: List<Profile>) {
        evaluationInputView.showDynamicEvaluationInput()
        evaluationInputView.showProfileSelector(profiles)

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
                return
            }

            // 평가 보고서 생성
            val report = reportModel.generateEvaluationReport(
                evaluatedName,
                profile
            ).getOrThrow()

            // 결과 표시
            handleReportGeneration(report)

        } catch (e: Exception) {
            evaluationInputView.showError("평가 실패: ${e.message}")
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