// model/domain/name/value/NameAnalysisRequest.kt
package com.ssc.namespring.model.domain.name.value

import com.ssc.namespring.model.domain.saju.entity.BirthDateTime
import com.ssc.namespring.model.domain.saju.entity.Saju
import com.ssc.namespring.model.domain.element.entity.ElementBalance

data class NameAnalysisRequest(
    val surHangul: String?,
    val surHanja: String,
    val name1Hangul: String?,
    val name1Hanja: String?,
    val name2Hangul: String?,
    val name2Hanja: String?,
    val birthDateTime: BirthDateTime,
    val saju: Saju? = null,
    val elementBalance: ElementBalance? = null
)