// model/domain/name/value/NameReport.kt
package com.ssc.namespring.model.domain.name.value

data class NameReport(
    val summary: String,
    val sajuAnalysis: String,
    val strokeAnalysis: String,
    val elementHarmony: String,
    val yinYangBalance: String,
    val pronunciationAnalysis: String,
    val overallEvaluation: String,
    val recommendations: List<String>
)