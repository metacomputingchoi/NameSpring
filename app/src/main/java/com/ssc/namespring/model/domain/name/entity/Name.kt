// model/domain/name/entity/Name.kt
package com.ssc.namespring.model.domain.name.entity

import com.ssc.namespring.model.domain.saju.entity.BirthDateTime
import com.ssc.namespring.model.domain.saju.entity.Saju
import com.ssc.namespring.model.domain.element.entity.ElementBalance
import com.ssc.namespring.model.domain.hanja.entity.Hanja

data class Name(
    val surHangul: String,
    val surHanja: String,
    val surHangulElement: String?,
    val surHangulPm: Int,
    val birthInfo: BirthDateTime,
    val sajuInfo: Saju,
    val dictElementsCount: ElementBalance,
    val zeroElements: List<String>,
    val oneElements: List<String>,
    val combinationAnalysis: NameCombination,
    val hanja1Info: Hanja,
    val hanja2Info: Hanja,
    val filteringProcess: MutableList<FilteringStep>,
    var combinedElement: String? = null,
    var combinedPm: String? = null,
    var combinedHanja: String? = null,
    var combinedPronounciation: String? = null
)