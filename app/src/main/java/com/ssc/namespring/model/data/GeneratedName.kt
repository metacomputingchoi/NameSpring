// model/data/GeneratedName.kt
package com.ssc.namespring.model.data

import com.ssc.namespring.model.data.analysis.NameAnalysisInfo

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