// data/analysis/NameAnalysisInfo.kt
package com.ssc.namingengine.data.analysis

import com.ssc.namingengine.data.analysis.component.SajuAnalysisInfo
import com.ssc.namingengine.data.analysis.component.EumYangAnalysisInfo
import com.ssc.namingengine.data.analysis.component.OhaengAnalysisInfo

data class NameAnalysisInfo(
    val sajuInfo: SajuAnalysisInfo,
    val eumYangInfo: EumYangAnalysisInfo,
    val ohaengInfo: OhaengAnalysisInfo,
    val filteringSteps: List<FilteringStep>,
    val totalScore: Int,
    val scoreBreakdown: Map<String, Int>,
    val recommendations: List<String>
)
