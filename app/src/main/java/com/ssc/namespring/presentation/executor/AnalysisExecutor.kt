// presentation/executor/AnalysisExecutor.kt
package com.ssc.namespring.presentation.executor

import android.content.Context
import com.ssc.namespring.model.application.service.AnalysisService
import com.ssc.namespring.model.data.AnalysisResult
import kotlinx.coroutines.*

class AnalysisExecutor(context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val analysisService = AnalysisService(context)

    fun executeAnalysis(
        surHangul: String,
        surHanja: String,
        name1Hangul: String?,
        name1Hanja: String?,
        name2Hangul: String?,
        name2Hanja: String?,
        birthYear: Int,
        birthMonth: Int,
        birthDay: Int,
        birthHour: Int,
        birthMinute: Int,
        targetName: String? = null,
        onComplete: (AnalysisResult) -> Unit
    ) {
//        // 시리얼 버전
//        val result = analysisService.analyzeNames(
//            surHangul, surHanja, name1Hangul, name1Hanja,
//            name2Hangul, name2Hanja, birthYear, birthMonth,
//            birthDay, birthHour, birthMinute, targetName
//        )
//        onComplete(result)

        // 코루틴 버전
        scope.launch {
            val result = analysisService.analyzeNames(
                surHangul, surHanja, name1Hangul, name1Hanja,
                name2Hangul, name2Hanja, birthYear, birthMonth,
                birthDay, birthHour, birthMinute, targetName
            )
            withContext(Dispatchers.Main) {
                onComplete(result)
            }
        }
    }

    fun cleanup() {
        scope.cancel()
    }
}