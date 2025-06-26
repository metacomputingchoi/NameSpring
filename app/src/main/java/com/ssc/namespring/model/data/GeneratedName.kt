// model/data/GeneratedName.kt
package com.ssc.namespring.model.data

import com.ssc.namespring.model.data.analysis.NameAnalysisInfo

// 피드백: has-a 관계를 명확히 하여 데이터 클래스 구조 개선
data class GeneratedName(
    val surnameHangul: String,
    val surnameHanja: String,
    val combinedHanja: String,
    val combinedPronounciation: String,
    val sagyeok: Sagyeok,
    val nameHanjaHoeksu: List<Int>,
    val hanjaDetails: List<HanjaInfo>,

    // 분석 정보 (lazy하게 생성)
    var analysisInfo: NameAnalysisInfo? = null
)
