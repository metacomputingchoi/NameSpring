// model/test/TestCaseRunner.kt
package com.ssc.namespring.model.test

import android.content.Context
import com.ssc.namespring.model.application.service.AnalysisService
import com.ssc.namespring.model.application.service.report.ReportService
import com.ssc.namespring.model.application.service.ScoreCalculationService
import com.ssc.namespring.model.common.constants.Constants
import com.ssc.namespring.model.data.*
import com.ssc.namespring.presentation.presenter.TestPresenter
import com.ssc.namespring.presentation.presenter.AnalysisPresenter

class TestCaseRunner(context: Context) {
    private val analysisService = AnalysisService(context)
    private val testCaseLoader = TestCaseLoader(context)
    private val testPresenter = TestPresenter()
    private val reportService = ReportService(context, ScoreCalculationService())
    private val analysisPresenter = AnalysisPresenter(analysisService, reportService)

    fun runTestCases(configFileName: String = "testcases.json"): TestSuiteResult {
        val config = testCaseLoader.loadTestConfig(configFileName)
        val suiteStartTime = System.currentTimeMillis()

        val fourJu = analysisService.getSaju(
            config.birthInfo.year,
            config.birthInfo.month,
            config.birthInfo.day,
            config.birthInfo.hour,
            config.birthInfo.minute
        )
        val dictElementsCount = analysisService.getElementBalance(fourJu)

        testPresenter.presentTestSummary(config.targetName)

        val testResults = config.testCases.mapIndexed { idx, testCase ->
            runSingleTestCase(testCase, idx, config)
        }

        val summary = TestSummary(
            totalTests = testResults.size,
            successCount = testResults.count { it.error == null },
            errorCount = testResults.count { it.error != null },
            targetFoundCount = testResults.count { it.targetFound }
        )

        return TestSuiteResult(
            testResults = testResults,
            summary = summary,
            executionTimeMs = System.currentTimeMillis() - suiteStartTime,
            fourJu = fourJu,
            dictElementsCount = dictElementsCount,
            targetName = config.targetName,
            targetCaseIndex = config.targetCaseIndex
        )
    }

    private fun runSingleTestCase(
        testCase: TestCase,
        index: Int,
        config: TestConfig
    ): TestCaseResult {
        val testStartTime = System.currentTimeMillis()
        testPresenter.presentTestCaseStart(testCase)

        return try {
            val result = analysisService.analyzeNames(
                testCase.surHangul, testCase.surHanja,
                testCase.name1Hangul, testCase.name1Hanja,
                testCase.name2Hangul, testCase.name2Hanja,
                config.birthInfo.year, config.birthInfo.month,
                config.birthInfo.day, config.birthInfo.hour,
                config.birthInfo.minute, config.targetName
            )

            testPresenter.presentTotalResults(result.totalNames)

            if (result.targetFound) {
                testPresenter.presentTargetFound(config.targetName)
                if (index == config.targetCaseIndex) {
                    analysisPresenter.presentDetailedNameReport(result.targetNameInfo!!)
                } else {
                    testPresenter.presentTargetScore(result.targetNameInfo!!, result.targetScore!!.total)
                }
            } else {
                testPresenter.presentTargetNotFound(config.targetName)
            }

            val topResults = if (index != config.targetCaseIndex && result.names.isNotEmpty()) {
                result.names.take(Constants.TOP_RESULTS_COUNT).map { name ->
                    val score = analysisService.calculateDetailedScore(name)
                    NameSummary(
                        hanja = name.combinedHanja,
                        pronunciation = name.combinedPronounciation,
                        score = score.total
                    )
                }.also { testPresenter.presentTopResults(it) }
            } else emptyList()

            TestCaseResult(
                testCase = testCase,
                index = index,
                totalResults = result.totalNames,
                targetFound = result.targetFound,
                targetNameInfo = result.targetNameInfo,
                targetScore = result.targetScore?.total,
                topResults = topResults,
                executionTimeMs = System.currentTimeMillis() - testStartTime
            )
        } catch (e: Exception) {
            testPresenter.presentError(e.message)
            e.printStackTrace()

            TestCaseResult(
                testCase = testCase,
                index = index,
                totalResults = 0,
                targetFound = false,
                error = e.message,
                executionTimeMs = System.currentTimeMillis() - testStartTime
            )
        }
    }
}