// utils/NameGeneratorTester.kt
package com.ssc.namespring.utils

import android.util.Log
import com.ssc.namespring.model.core.NamingSystem
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

        // 어느 language를 가든 공통적으로 잘 지원하는 놈들은 찾아서 쓰는게 좋다.
        // Calendar
        // String(char)
        // Timer
        // Thread
        // ...
        // Algorithm/DS

        // 이런걸 활용해서 뭘 만들어놓은 외부라이브러리들은 안쓰는게 공부할땐 좋다
        // 이미 어떤 목적성을 가지고 구현 완료해놓은 gh 프로젝트들을 갖다쓰는건 내가 진짜 개발목적으로 밥벌이 할때 쓰는거

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