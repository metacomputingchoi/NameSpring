// presentation/TestExecutor.kt
package com.ssc.namespring.presentation

import android.content.Context
import com.ssc.namespring.model.data.TestSuiteResult
import com.ssc.namespring.model.test.TestCaseRunner
import kotlinx.coroutines.*

class TestExecutor(context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val testCaseRunner = TestCaseRunner(context)

    fun executeTests(
        configFileName: String = "testcases.json",
        onComplete: (TestSuiteResult) -> Unit
    ) {
//        // 시리얼 버전
//        val results = testCaseRunner.runTestCases(configFileName)
//        onComplete(results)

        // 코루틴 버전
        scope.launch {
            val results = testCaseRunner.runTestCases(configFileName)
            withContext(Dispatchers.Main) {
                onComplete(results)
            }
        }
    }

    fun cleanup() {
        scope.cancel()
    }
}
