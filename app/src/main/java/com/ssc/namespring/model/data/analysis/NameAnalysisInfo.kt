// model/data/analysis/NameAnalysisInfo.kt
package com.ssc.namespring.model.data.analysis

import com.ssc.namespring.model.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.data.analysis.component.EumYangAnalysisInfo
import com.ssc.namespring.model.data.analysis.component.OhaengAnalysisInfo

data class NameAnalysisInfo(
    val sajuInfo: SajuAnalysisInfo,
    val eumYangInfo: EumYangAnalysisInfo,
    val ohaengInfo: OhaengAnalysisInfo,
    val filteringSteps: List<FilteringStep>,
    val totalScore: Int,
    val scoreBreakdown: Map<String, Int>,
    val recommendations: List<String>
)
