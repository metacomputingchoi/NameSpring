// MainActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.presentation.TestExecutor
import com.ssc.namespring.presentation.AnalysisExecutor

class MainActivity : AppCompatActivity() {

    private lateinit var testExecutor: TestExecutor
    private lateinit var analysisExecutor: AnalysisExecutor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        testExecutor = TestExecutor(this)
        analysisExecutor = AnalysisExecutor(this)

        testExecutor.executeTests("testcases.json") { result ->
            Log.i("TestResult", "총 테스트: ${result.summary.totalTests}")
            Log.i("TestResult", "타겟 발견 횟수: ${result.summary.targetFoundCount}")
        }

        analysisExecutor.executeAnalysis { result ->
            Log.i("AnalysisResult: ", result.elementBalance.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        testExecutor.cleanup()
        analysisExecutor.cleanup()
    }
}