// presentation/AnalysisExecutor.kt
package com.ssc.namespring.presentation

import android.content.Context
import com.ssc.namespring.model.application.facade.NameAnalyzer
import com.ssc.namespring.model.data.AnalysisResult
import kotlinx.coroutines.*

class AnalysisExecutor(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun executeAnalysis(onComplete: (AnalysisResult) -> Unit) {
//        // 시리얼 버전
//        val analyzer = NameAnalyzer(context)
//        val result = analyzer.runAnalysisWithBuilder()
//        onComplete(result)

        // 코루틴 버전
        scope.launch {
            val analyzer = NameAnalyzer(context)
            val result = analyzer.runAnalysisWithBuilder()
            withContext(Dispatchers.Main) {
                onComplete(result)
            }
        }
    }

    fun cleanup() {
        scope.cancel()
    }
}
