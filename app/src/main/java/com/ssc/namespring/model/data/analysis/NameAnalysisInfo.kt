// model/data/analysis/NameAnalysisInfo.kt
package com.ssc.namespring.model.data.analysis

import com.ssc.namespring.model.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.data.analysis.component.YinYangAnalysisInfo
import com.ssc.namespring.model.data.analysis.component.OhaengAnalysisInfo

data class NameAnalysisInfo(
    val sajuInfo: SajuAnalysisInfo,
    val yinYangInfo: YinYangAnalysisInfo,
    val ohaengInfo: OhaengAnalysisInfo,
    val filteringSteps: List<FilteringStep>,
    val totalScore: Int,
    val scoreBreakdown: Map<String, Int>,
    val recommendations: List<String>
)
