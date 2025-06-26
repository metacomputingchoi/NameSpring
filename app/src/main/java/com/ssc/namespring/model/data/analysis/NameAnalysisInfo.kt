// model/data/analysis/NameAnalysisInfo.kt
package com.ssc.namespring.model.data.analysis

import com.ssc.namespring.model.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.data.analysis.component.YinYangAnalysisInfo
import com.ssc.namespring.model.data.analysis.component.OhaengAnalysisInfo

// 피드백: 분석 정보를 별도 패키지로 분리하여 구조 명확화
data class NameAnalysisInfo(
    // 사주 정보
    val sajuInfo: SajuAnalysisInfo,

    // 음양 분석
    val yinYangInfo: YinYangAnalysisInfo,

    // 오행 분석
    val ohaengInfo: OhaengAnalysisInfo,

    // 필터링 과정
    val filteringSteps: List<FilteringStep>,

    // 종합 점수 및 평가
    val totalScore: Int,
    val scoreBreakdown: Map<String, Int>,
    val recommendations: List<String>
)
