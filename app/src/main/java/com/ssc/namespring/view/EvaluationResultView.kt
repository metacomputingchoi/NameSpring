// view/EvaluationResultView.kt
package com.ssc.namespring.view

import com.ssc.namespring.model.data.EvaluationReport

interface EvaluationResultView {
    fun showOverallScore(score: Int)
    fun showRadarChart(scores: Map<String, Int>)
    fun showDetailedAnalysis(report: EvaluationReport)
    fun showRecommendations(recommendations: List<String>)
    fun showImprovements(improvements: List<String>)
    fun showSaveOptions()
    fun showShareOptions()
    fun applyScoreTheme(score: Int)
    fun showError(message: String)
}