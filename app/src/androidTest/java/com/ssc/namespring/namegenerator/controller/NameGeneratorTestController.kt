// src/androidTest/java/com/ssc/namespring/namegenerator/controller/NameGeneratorTestController.kt
package com.ssc.namespring.namegenerator.controller

import android.util.Log
import com.ssc.namespring.model.NameGeneratorModel
import com.ssc.namespring.namegenerator.model.TestCase
import com.ssc.namespring.namegenerator.model.TestConfiguration
import com.ssc.namespring.namegenerator.model.TestResult
import com.ssc.namespring.namegenerator.view.TestResultView
import com.ssc.namingengine.exception.NamingEngineException
import java.time.LocalDateTime

class NameGeneratorTestController(
    private val model: NameGeneratorModel,
    private val view: TestResultView
) {

    companion object {
        private const val TAG = "NameGeneratorTestController"
    }

    fun runAllTests(configurations: List<TestConfiguration>) {
        configurations.forEach { config ->
            runTestConfiguration(config)
        }
    }

    fun runTestConfiguration(configuration: TestConfiguration) {
        view.showTestTypeHeader(
            configuration.testType,
            model.formatDateTime(configuration.defaultBirthDateTime)
        )

        val testResults = mutableListOf<TestResult>()

        configuration.testCases.forEach { testCase ->
            val result = runSingleTest(testCase, configuration.defaultBirthDateTime)
            testResults.add(result)
        }

        view.showTestSummary(testResults)
    }

    fun runSingleTest(
        testCase: TestCase,
        defaultBirthDateTime: LocalDateTime
    ): TestResult {
        val birthDateTime = testCase.birthDateTime ?: defaultBirthDateTime

        view.showTestHeader(testCase.input, testCase.description)

        return try {
            val validationResult = model.validateInput(testCase.input)
            if (!validationResult.isValid) {
                view.showError("입력 검증 실패: ${validationResult.errorMessage}")
                return TestResult(
                    testCase = testCase,
                    results = emptyList(),
                    elapsedTime = 0,
                    error = validationResult.errorMessage
                )
            }

            val startTime = System.currentTimeMillis()

            val results = model.generateNames(
                userInput = testCase.input,
                birthDateTime = birthDateTime,
                useYajasi = true,
                verbose = true,
                withoutFilter = testCase.withoutFilter
            )

            val elapsedTime = System.currentTimeMillis() - startTime

            if (testCase.withoutFilter) {
                view.showEvaluationResults(results, elapsedTime)
            } else {
                view.showGenerationResults(results, elapsedTime)
            }

            TestResult(
                testCase = testCase,
                results = results,
                elapsedTime = elapsedTime
            )

        } catch (e: NamingEngineException) {
            val errorMessage = "에러 발생: ${e.message}"
            view.showError(errorMessage)
            TestResult(
                testCase = testCase,
                results = emptyList(),
                elapsedTime = 0,
                error = errorMessage
            )
        } catch (e: Exception) {
            val errorMessage = "예상치 못한 에러 발생: ${e.message}"
            view.showError(errorMessage)
            Log.e(TAG, "테스트 실행 중 오류", e)
            TestResult(
                testCase = testCase,
                results = emptyList(),
                elapsedTime = 0,
                error = errorMessage
            )
        } finally {
            println()
        }
    }
}