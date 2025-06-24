// model/domain/name/builder/NameAnalysisRequestBuilder.kt
package com.ssc.namespring.model.domain.name.builder

import com.ssc.namespring.model.domain.name.value.NameAnalysisRequest
import com.ssc.namespring.model.domain.saju.entity.BirthDateTime
import com.ssc.namespring.model.domain.saju.entity.Saju
import com.ssc.namespring.model.domain.element.entity.ElementBalance
import com.ssc.namespring.model.common.constants.Constants

class NameAnalysisRequestBuilder {
    private var surHangul: String? = null
    private var surHanja: String = ""
    private var name1Hangul: String? = null
    private var name1Hanja: String? = null
    private var name2Hangul: String? = null
    private var name2Hanja: String? = null
    private var birthDateTime: BirthDateTime? = null
    private var saju: Saju? = null
    private var elementBalance: ElementBalance? = null

    fun withSurname(hangul: String?, hanja: String) = apply {
        this.surHangul = hangul
        this.surHanja = hanja
    }

    fun withFirstName(hangul: String?, hanja: String?) = apply {
        this.name1Hangul = hangul
        this.name1Hanja = hanja
    }

    fun withSecondName(hangul: String?, hanja: String?) = apply {
        this.name2Hangul = hangul
        this.name2Hanja = hanja
    }

    fun withBirthInfo(year: Int, month: Int, day: Int, hour: Int, minute: Int) = apply {
        this.birthDateTime = BirthDateTime(year, month, day, hour, minute)
    }

    fun withSaju(saju: Saju) = apply {
        this.saju = saju
    }

    fun withElementBalance(elementBalance: ElementBalance?) = apply {
        this.elementBalance = elementBalance
    }

    fun build(): NameAnalysisRequest {
        val finalBirthDateTime = birthDateTime ?: BirthDateTime(
            Constants.DEFAULT_BIRTH_YEAR,
            Constants.DEFAULT_BIRTH_MONTH,
            Constants.DEFAULT_BIRTH_DAY,
            Constants.DEFAULT_BIRTH_HOUR,
            Constants.DEFAULT_BIRTH_MINUTE
        )

        return NameAnalysisRequest(
            surHangul = surHangul,
            surHanja = surHanja,
            name1Hangul = name1Hangul,
            name1Hanja = name1Hanja,
            name2Hangul = name2Hangul,
            name2Hanja = name2Hanja,
            birthDateTime = finalBirthDateTime,
            saju = saju,
            elementBalance = elementBalance
        )
    }
}