// utils/NameGeneratorTester.kt
package com.ssc.namespring.utils

import android.util.Log
import com.ssc.namespring.model.core.NamingSystem
import com.ssc.namespring.model.exception.NamingException

class NameGeneratorTester(
    private val namingSystem: NamingSystem  // 생성자 주입으로 받음
) {

    companion object {
        private const val TAG = "NameGeneratorTester"

        // 테스트 데이터
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

        private const val TEST_BIRTH_YEAR = 2025
        private const val TEST_BIRTH_MONTH = 6
        private const val TEST_BIRTH_DAY = 11
        private const val TEST_BIRTH_HOUR = 14
        private const val TEST_BIRTH_MINUTE = 30
    }

    private val resultFormatter = TestResultFormatter()

    fun runAllTests() {  // suspend 제거
        TEST_INPUTS.forEach { testInput ->
            testSingleInput(testInput)
        }
    }

    private fun testSingleInput(testInput: String) {  // suspend 제거
        resultFormatter.printTestHeader(testInput)

        try {
            val startTime = System.currentTimeMillis()

            // 이미 백그라운드 스레드에서 실행되므로 withContext 불필요
            val results = namingSystem.generateKoreanNames(
                userInput = testInput,
                birthYear = TEST_BIRTH_YEAR,
                birthMonth = TEST_BIRTH_MONTH,
                birthDay = TEST_BIRTH_DAY,
                birthHour = TEST_BIRTH_HOUR,
                birthMinute = TEST_BIRTH_MINUTE,
                verbose = true
            )

            val elapsedTime = System.currentTimeMillis() - startTime

            resultFormatter.printTestResults(results, elapsedTime)

        } catch (e: NamingException) {
            resultFormatter.printError("에러 발생: ${e.message}")
        } catch (e: Exception) {
            resultFormatter.printError("예상치 못한 에러 발생: ${e.message}")
            Log.e(TAG, "테스트 실행 중 오류", e)
        }

        println()
    }
}