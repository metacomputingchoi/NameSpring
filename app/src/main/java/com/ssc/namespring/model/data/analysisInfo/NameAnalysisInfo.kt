package com.ssc.namespring.model.data.analysisInfo

import com.ssc.namespring.model.data.FilteringStep
import com.ssc.namespring.model.data.OhaengAnalysisInfo
import com.ssc.namespring.model.data.SajuAnalysisInfo
import com.ssc.namespring.model.data.YinYangAnalysisInfo

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