// utils/NameGeneratorTester.kt
package com.ssc.namespring.utils

import android.util.Log
import com.ssc.namespring.model.core.NamingSystem
import com.ssc.namespring.model.exception.NamingException
import java.time.LocalDateTime

class NameGeneratorTester(
    private val namingSystem: NamingSystem  // 생성자 주입으로 받음
) {

    companion object {
        private const val TAG = "NameGeneratorTester"

        private val TEST_BIRTH_DATETIME = LocalDateTime.of(2025, 6, 11, 14, 30, 0)
        private val TEST_INPUTS = listOf(
            "[김/金][_/_]",
            "[김/金][_/_][_/_]",
            "[김/金][ㅁ/_][_/_]",
            "[김/金][_/_][ㄱ/_]",
            "[김/金][민/_][_/_]",
            "[김/金][_/岷][_/_]",
            "[김/金][민/岷][_/_]",
            "[남궁/南宮][_/_][_/_]",
            "[남궁/南宮][_/_]",
            "[김/金][민/岷][구/枸]"
        )

        private val EVALUATION_TEST_BIRTH_DATETIME = LocalDateTime.of(1986, 4, 19, 5, 45, 0)
        private val EVALUATION_TEST_INPUTS = listOf(
            TestCase(
                input = "[최/崔][성/成][수/_]",
                description = "최성수 - 높을 최(崔), 이룰 성(成), 수(한자 비움)"
            ),
            TestCase(
                input = "[최/崔][성/成][수/秀]",
                description = "최성수 - 높을 최(崔), 이룰 성(成), 빼어날 수(秀)"
            )
        )

        data class TestCase(
            val input: String,
            val description: String
        )
    }

    private val resultFormatter = TestResultFormatter()

    fun runAllTests() {
        // 필터링 생성 테스트
        println("====== 필터링 생성 테스트 시작 ======")
        println("생년월일시분: 2025년 6월 11일 오후 2시 30분")
        TEST_INPUTS.forEach { testInput ->
            testSingleInput(testInput, withoutFilter = false)
        }

        // 생성평가 테스트
        println("\n\n====== 생성 평가 테스트 시작 ======")
        println("생년월일시분: 1986년 4월 19일 오전 5시 45분")
        runEvaluationTests()
    }

    fun runEvaluationTests() {
        EVALUATION_TEST_INPUTS.forEach { testCase ->
            println("\n--- ${testCase.description} ---")
            testSingleInput(
                testInput = testCase.input,
                birthDateTime = EVALUATION_TEST_BIRTH_DATETIME,
                withoutFilter = true,
                showDescription = testCase.description
            )
        }
    }

    private fun testSingleInput(
        testInput: String,
        birthDateTime: LocalDateTime = TEST_BIRTH_DATETIME,
        withoutFilter: Boolean = false,
        showDescription: String? = null
    ) {
        if (showDescription != null) {
            resultFormatter.printTestHeader("$testInput\n설명: $showDescription")
        } else {
            resultFormatter.printTestHeader(testInput)
        }

        try {
            val startTime = System.currentTimeMillis()

            val results = namingSystem.generateKoreanNames(
                userInput = testInput,
                birthDateTime = birthDateTime,
                useYajasi = true,
                verbose = true,
                withoutFilter = withoutFilter
            )

            val elapsedTime = System.currentTimeMillis() - startTime

            if (withoutFilter) {
                resultFormatter.printEvaluationResults(results, elapsedTime)
            } else {
                resultFormatter.printTestResults(results, elapsedTime)
            }

        } catch (e: NamingException) {
            resultFormatter.printError("에러 발생: ${e.message}")
        } catch (e: Exception) {
            resultFormatter.printError("예상치 못한 에러 발생: ${e.message}")
            Log.e(TAG, "테스트 실행 중 오류", e)
        }

        println()
    }
}