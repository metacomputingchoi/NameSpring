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
    val analysisInfo: NameAnalysisInfo? = null
) {
    fun withAnalysisInfo(info: NameAnalysisInfo): GeneratedName {
        return copy(analysisInfo = info)
    }

    fun toBuilder() = Builder(this)

    class Builder(
        private var surnameHangul: String = "",
        private var surnameHanja: String = "",
        private var combinedHanja: String = "",
        private var combinedPronounciation: String = "",
        private var sagyeok: Sagyeok? = null,
        private var nameHanjaHoeksu: List<Int> = emptyList(),
        private var hanjaDetails: List<HanjaInfo> = emptyList(),
        private var analysisInfo: NameAnalysisInfo? = null
    ) {
        constructor(name: GeneratedName) : this(
            name.surnameHangul,
            name.surnameHanja,
            name.combinedHanja,
            name.combinedPronounciation,
            name.sagyeok,
            name.nameHanjaHoeksu,
            name.hanjaDetails,
            name.analysisInfo
        )

        fun surnameHangul(value: String) = apply { surnameHangul = value }
        fun surnameHanja(value: String) = apply { surnameHanja = value }
        fun combinedHanja(value: String) = apply { combinedHanja = value }
        fun combinedPronounciation(value: String) = apply { combinedPronounciation = value }
        fun sagyeok(value: Sagyeok) = apply { sagyeok = value }
        fun nameHanjaHoeksu(value: List<Int>) = apply { nameHanjaHoeksu = value }
        fun hanjaDetails(value: List<HanjaInfo>) = apply { hanjaDetails = value }
        fun analysisInfo(value: NameAnalysisInfo?) = apply { analysisInfo = value }

        fun build(): GeneratedName {
            return GeneratedName(
                surnameHangul = surnameHangul,
                surnameHanja = surnameHanja,
                combinedHanja = combinedHanja,
                combinedPronounciation = combinedPronounciation,
                sagyeok = sagyeok ?: throw IllegalStateException("Sagyeok is required"),
                nameHanjaHoeksu = nameHanjaHoeksu,
                hanjaDetails = hanjaDetails,
                analysisInfo = analysisInfo
            )
        }
    }
}