// model/NameGeneratorTester.kt
package com.ssc.namespring.model

import android.util.Log
import com.ssc.namingengine.NamingEngine
import com.ssc.namingengine.exception.NamingEngineException
import java.time.LocalDateTime

class NameGeneratorTester(
    private val namingEngine: NamingEngine
) {

    companion object {
        private const val TAG = "NameGeneratorTester"

        fun getDefaultTestConfigurations(): List<TestConfiguration> {
            return listOf(
                TestConfiguration(
                    testCases = listOf(
                        TestCase("[김/金][_/_]"),
                        TestCase("[김/金][_/_][_/_]"),
                        TestCase("[김/金][ㅁ/_][_/_]"),
                        TestCase("[김/金][_/_][ㄱ/_]"),
                        TestCase("[김/金][민/_][_/_]"),
                        TestCase("[김/金][_/岷][_/_]"),
                        TestCase("[김/金][민/岷][_/_]"),
                        TestCase("[남궁/南宮][_/_][_/_]"),
                        TestCase("[남궁/南宮][_/_]"),
                        TestCase("[김/金][민/岷][구/枸]")
                    ),
                    defaultBirthDateTime = LocalDateTime.of(2025, 6, 11, 14, 30, 0),
                    testType = TestType.GENERATION
                ),
                TestConfiguration(
                    testCases = listOf(
                        TestCase(
                            input = "[최/崔][성/成][수/_]",
                            description = "최성수 - 높을 최(崔), 이룰 성(成), 수(한자 비움)",
                            withoutFilter = true
                        ),
                        TestCase(
                            input = "[최/崔][성/成][수/秀]",
                            description = "최성수 - 높을 최(崔), 이룰 성(成), 빼어날 수(秀)",
                            withoutFilter = true
                        )
                    ),
                    defaultBirthDateTime = LocalDateTime.of(1986, 4, 19, 5, 45, 0),
                    testType = TestType.EVALUATION
                ),
                TestConfiguration(
                    testCases = listOf(
                        TestCase(
                            input = "[김/金][우/禹][현/鉉]",
                            description = "김우현 - 쇠 김(金), 하우씨 우(禹), 솥귀 현(鉉)",
                            withoutFilter = true
                        )
                    ),
                    defaultBirthDateTime = LocalDateTime.of(1989, 1, 10, 1, 15, 0),
                    testType = TestType.EVALUATION
                )
            )
        }
    }

    private val resultFormatter = TestResultFormatter()

    fun runAllTests() {
        val configurations = getDefaultTestConfigurations()
        configurations.forEach { config ->
            runTestConfiguration(config)
        }
    }

    fun runTestConfiguration(configuration: TestConfiguration) {
        when (configuration.testType) {
            TestType.GENERATION -> {
                println("====== 필터링 생성 테스트 시작 ======")
                println("생년월일시분: ${formatDateTime(configuration.defaultBirthDateTime)}")
            }
            TestType.EVALUATION -> {
                println("\n\n====== 생성 평가 테스트 시작 ======")
                println("생년월일시분: ${formatDateTime(configuration.defaultBirthDateTime)}")
            }
        }

        val testResults = mutableListOf<TestResult>()

        configuration.testCases.forEach { testCase ->
            val result = runSingleTest(testCase, configuration.defaultBirthDateTime)
            testResults.add(result)
        }

        resultFormatter.printTestSummary(testResults)
    }

    fun runSingleTest(
        testCase: TestCase,
        defaultBirthDateTime: LocalDateTime
    ): TestResult {
        val birthDateTime = testCase.birthDateTime ?: defaultBirthDateTime

        if (testCase.description != null) {
            resultFormatter.printTestHeader("${testCase.input}\n설명: ${testCase.description}")
        } else {
            resultFormatter.printTestHeader(testCase.input)
        }

        return try {
            // 입력 검증
            val validationResult = namingEngine.validateInput(testCase.input)
            if (!validationResult.isValid) {
                resultFormatter.printError("입력 검증 실패: ${validationResult.errorMessage}")
                return TestResult(
                    testCase = testCase,
                    results = emptyList(),
                    elapsedTime = 0,
                    error = validationResult.errorMessage
                )
            }

            val startTime = System.currentTimeMillis()

            // 이름 생성
            val results = namingEngine.generateNames(
                userInput = testCase.input,
                birthDateTime = birthDateTime,
                useYajasi = true,
                verbose = true,
                withoutFilter = testCase.withoutFilter
            )

            val elapsedTime = System.currentTimeMillis() - startTime

            if (testCase.withoutFilter) {
                resultFormatter.printEvaluationResults(results, elapsedTime)
            } else {
                resultFormatter.printTestResults(results, elapsedTime)
            }

            TestResult(
                testCase = testCase,
                results = results,
                elapsedTime = elapsedTime
            )

        } catch (e: NamingEngineException) {
            val errorMessage = "에러 발생: ${e.message}"
            resultFormatter.printError(errorMessage)
            TestResult(
                testCase = testCase,
                results = emptyList(),
                elapsedTime = 0,
                error = errorMessage
            )
        } catch (e: Exception) {
            val errorMessage = "예상치 못한 에러 발생: ${e.message}"
            resultFormatter.printError(errorMessage)
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

    fun runCustomTest(
        userInput: String,
        birthDateTime: LocalDateTime,
        withoutFilter: Boolean = false,
        description: String? = null
    ): TestResult {
        val testCase = TestCase(
            input = userInput,
            description = description,
            birthDateTime = birthDateTime,
            withoutFilter = withoutFilter
        )
        return runSingleTest(testCase, birthDateTime)
    }

    private fun formatDateTime(dateTime: LocalDateTime): String {
        return "${dateTime.year}년 ${dateTime.monthValue}월 ${dateTime.dayOfMonth}일 " +
                "${if (dateTime.hour < 12) "오전" else "오후"} ${dateTime.hour % 12}시 ${dateTime.minute}분"
    }
}