// utils/NameGeneratorTester.kt
package com.ssc.namespring.utils

import android.util.Log
import com.ssc.namespring.model.core.NamingSystem
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.exception.NamingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NameGeneratorTester {

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

    private val namingSystem = NamingSystem.instance
    private val resultFormatter = TestResultFormatter()

    suspend fun runAllTests() {
        TEST_INPUTS.forEach { testInput ->
            testSingleInput(testInput)
        }
    }

    private suspend fun testSingleInput(testInput: String) {
        resultFormatter.printTestHeader(testInput)

        try {
            val startTime = System.currentTimeMillis()

            val results = withContext(Dispatchers.Default) {
                namingSystem.generateKoreanNames(
                    userInput = testInput,
                    birthYear = TEST_BIRTH_YEAR,
                    birthMonth = TEST_BIRTH_MONTH,
                    birthDay = TEST_BIRTH_DAY,
                    birthHour = TEST_BIRTH_HOUR,
                    birthMinute = TEST_BIRTH_MINUTE,
                    verbose = true
                )
            }

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