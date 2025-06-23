// model/application/test/TestCaseRunner.kt
package com.ssc.namespring.model.application.test

import android.content.Context
import com.ssc.namespring.model.application.facade.NameAnalyzer
import com.ssc.namespring.model.common.constants.NameAnalyzerConstants
import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.domain.element.entity.ElementBalance
import com.ssc.namespring.model.domain.name.entity.Name

class TestCaseRunner(context: Context) {

    private val nameAnalyzer = NameAnalyzer(context)
    private val testCaseLoader = TestCaseLoader(context)

    fun runTestCases(configFileName: String = "testcases.json"): TestSuiteResult {
        val config = testCaseLoader.loadTestConfig(configFileName)
        val suiteStartTime = System.currentTimeMillis()

        val fourJu = nameAnalyzer.get4Ju(
            config.birthInfo.year,
            config.birthInfo.month,
            config.birthInfo.day,
            config.birthInfo.hour,
            config.birthInfo.minute
        )
        val dictElementsCount = nameAnalyzer.getDictElementsCount(fourJu)

        val testResults = mutableListOf<TestCaseResult>()

        config.testCases.forEachIndexed { idx, testCase ->
            val result = runSingleTestCase(
                testCase,
                idx,
                dictElementsCount,
                config.targetName,
                config.targetCaseIndex,
                config.birthInfo
            )
            testResults.add(result)
        }

        printSummary(testResults, config.targetName)

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
        dictElementsCount: ElementBalance,
        targetName: String,
        targetCaseIndex: Int,
        birthInfo: BirthInfo
    ): TestCaseResult {
        val testStartTime = System.currentTimeMillis()

        println("\n${"=".repeat(NameAnalyzerConstants.SEPARATOR_LINE_LENGTH)}")
        println("테스트 케이스: ${testCase.name}")
        println("=".repeat(NameAnalyzerConstants.SEPARATOR_LINE_LENGTH))

        return try {
            val results = nameAnalyzer.findNamesGeneralized(
                testCase.surHangul, testCase.surHanja,
                testCase.name1Hangul, testCase.name1Hanja,
                testCase.name2Hangul, testCase.name2Hanja,
                dictElementsCount,
                birthInfo.year, birthInfo.month, birthInfo.day,
                birthInfo.hour, birthInfo.minute
            )

            println("총 결과 수: ${results.size}")

            val targetResult = findTargetName(results, targetName, index, targetCaseIndex)
            val topResults = getTopResults(results, index, targetCaseIndex)

            TestCaseResult(
                testCase = testCase,
                index = index,
                totalResults = results.size,
                targetFound = targetResult.first,
                targetNameInfo = targetResult.second,
                targetScore = targetResult.third,
                topResults = topResults,
                executionTimeMs = System.currentTimeMillis() - testStartTime
            )
        } catch (e: Exception) {
            println("오류 발생: ${e.message}")
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

    private fun findTargetName(
        results: List<Name>,
        targetName: String,
        index: Int,
        targetCaseIndex: Int
    ): Triple<Boolean, Name?, Int?> {
        for (result in results) {
            if (result.combinedHanja == targetName) {
                println("\n✓ ${targetName} 발견!")

                if (index == targetCaseIndex) {
                    nameAnalyzer.printDetailedNameReport(result)
                    return Triple(true, result, null)
                } else {
                    val score = nameAnalyzer.calculateDetailedScore(result).total
                    println("  ${result.combinedHanja} ${result.combinedPronounciation} - 총점: ${score}점")
                    return Triple(true, result, score)
                }
            }
        }

        println("\n✗ ${targetName}이 결과에 포함되지 않음!")
        return Triple(false, null, null)
    }

    private fun getTopResults(
        results: List<Name>,
        index: Int,
        targetCaseIndex: Int
    ): List<NameSummary> {
        if (results.isEmpty() || index == targetCaseIndex) {
            return emptyList()
        }

        println("\n결과 샘플 (상위 ${NameAnalyzerConstants.TOP_RESULTS_COUNT}개):")
        return results.take(NameAnalyzerConstants.TOP_RESULTS_COUNT).mapIndexed { i, result ->
            val score = nameAnalyzer.calculateDetailedScore(result).total
            println("${i + 1}. ${result.combinedHanja} ${result.combinedPronounciation} - 총점: ${score}점")
            NameSummary(
                hanja = result.combinedHanja,
                pronunciation = result.combinedPronounciation,
                score = score
            )
        }
    }

    private fun printSummary(testResults: List<TestCaseResult>, targetName: String) {
        println("\n${"=".repeat(NameAnalyzerConstants.SEPARATOR_LINE_LENGTH)}")
        println("테스트 결과 요약")
        println("=".repeat(NameAnalyzerConstants.SEPARATOR_LINE_LENGTH))
        println("모든 테스트 케이스에서 ${targetName}이 포함되는지 확인하십시오.")
        println("\n※ 성명학 점수는 참고용이며, 실제 이름 선택은 가족의 뜻과 개인의 선호를 종합적으로 고려하시기 바랍니다.")
    }
}