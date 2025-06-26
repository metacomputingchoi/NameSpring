// model/data/analysis/component/OhaengAnalysisInfo.kt
package com.ssc.namespring.model.data.analysis.component

data class OhaengAnalysisInfo(
    val baleumOhaeng: String,
    val hoeksuOhaeng: List<Int>,
    val jawonOhaeng: List<String>,
    val sagyeokSuriOhaeng: List<Int>,
    val harmonyScore: Int,
    val conflictingPairs: List<Pair<String, String>>,
    val generatingPairs: List<Pair<String, String>>,
    val overallHarmony: String
)
