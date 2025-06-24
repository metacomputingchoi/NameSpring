// MainActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.presentation.executor.TestExecutor
import com.ssc.namespring.presentation.executor.AnalysisExecutor
import com.ssc.namespring.presentation.presenter.AnalysisPresenter
import com.ssc.namespring.model.application.service.AnalysisService
import com.ssc.namespring.model.application.service.report.ReportService
import com.ssc.namespring.model.application.service.ScoreCalculationService

class MainActivity : AppCompatActivity() {

    private lateinit var testExecutor: TestExecutor
    private lateinit var analysisExecutor: AnalysisExecutor
    private lateinit var analysisPresenter: AnalysisPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        testExecutor = TestExecutor(this)
        analysisExecutor = AnalysisExecutor(this)

        val analysisService = AnalysisService(this)
        val reportService = ReportService(this, ScoreCalculationService())
        analysisPresenter = AnalysisPresenter(analysisService, reportService)

        testExecutor.executeTests("testcases.json") { result ->
            Log.i("TestResult", "총 테스트: ${result.summary.totalTests}")
            Log.i("TestResult", "타겟 발견 횟수: ${result.summary.targetFoundCount}")
        }

        // targetName 있는 경우
        analysisExecutor.executeAnalysis(
            surHangul = "김",
            surHanja = "金",
            name1Hangul = null,
            name1Hanja = "克",
            name2Hangul = null,
            name2Hanja = "訉",
            birthYear = 2025,
            birthMonth = 6,
            birthDay = 11,
            birthHour = 14,
            birthMinute = 30,
            targetName = "克訉"  // 특정 이름 찾기
        ) { result ->
            Log.i("AnalysisResult", "총 이름 수: ${result.totalNames}")
            Log.i("AnalysisResult", "타겟 발견: ${result.targetFound}")

            // 타겟이 발견된 경우 상세 보고서 생성
            if (result.targetFound && result.targetNameInfo != null) {
                analysisPresenter.presentDetailedNameReport(result.targetNameInfo)
            }

            analysisPresenter.presentNameList(result.names, 5)
        }

        // targetName 없는 경우 (전체 목록)
        analysisExecutor.executeAnalysis(
            surHangul = "이",
            surHanja = "李",
            name1Hangul = null,
            name1Hanja = null,
            name2Hangul = null,
            name2Hanja = null,
            birthYear = 2025,
            birthMonth = 7,
            birthDay = 1,
            birthHour = 10,
            birthMinute = 0,
            targetName = null  // 전체 목록 생성
        ) { result ->
            Log.i("AnalysisResult2", "총 생성된 이름 수: ${result.totalNames}")

            // 첫 번째 이름에 대한 기본 보고서 생성
            if (result.names.isNotEmpty()) {
                analysisPresenter.presentBasicNameReport(result.names.first())
            }

            analysisPresenter.presentNameList(result.names, 10)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        testExecutor.cleanup()
        analysisExecutor.cleanup()
    }
}