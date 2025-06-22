// model/data/DataClasses.kt
package com.ssc.namespring.model.data

data class YMDRecord(
    val year: Int, 
    val month: Int, 
    val day: Int,
    val yeonju: String, 
    val wolju: String, 
    val ilju: String
)

data class HanjaInfo(
    val hanja: String,
    val inmyeongYongEum: String?,
    val inmyeongYongDdeut: String?,
    val wonHoeksu: Int,
    val jawonOheng: String?,
    val baleumOheng: String?,
    val cautionRed: String?,
    val cautionBlue: String?
)

data class FourJu(
    val yeonju: String, 
    val wolju: String,
    val ilju: String, 
    val siju: String
)

data class ElementCount(
    val wood: Int, 
    val fire: Int, 
    val earth: Int,
    val metal: Int, 
    val water: Int
) {
    fun toMap() = mapOf(
        "木" to wood, 
        "火" to fire, 
        "土" to earth,
        "金" to metal, 
        "水" to water
    )
}

data class CombinationAnalysis(
    val surHanjaStroke: Int,
    val stroke1: Int,
    val stroke2: Int,
    val fourTypes: List<Int>,
    val fourTypesLuck: List<Int>,
    val initialScore: Int,
    val namePn: List<Int>,
    val namePnSum: Int,
    val nameElements: List<Int>,
    val nameElementChecks: List<ElementCheck>,
    val scoreCoexistName: Int,
    val typeElements: List<Int>,
    val typeElementChecks: List<ElementCheck>,
    val scoreCoexistType: Int,
    val finalScore: Int,
    val scoreZeroReason: String? = null
)

data class ElementCheck(
    val position: String,
    val elements: String,
    val diff: Int,
    val result: String
)

data class FilteringStep(
    val step: String,
    val passed: Boolean,
    val reason: String? = null,
    val details: Map<String, Any>? = null
)

data class NameResult(
    val surHangul: String,
    val surHanja: String,
    val surHangulElement: String?,
    val surHangulPm: Int,
    val birthInfo: BirthInfo,
    val sajuInfo: FourJu,
    val dictElementsCount: ElementCount,
    val zeroElements: List<String>,
    val oneElements: List<String>,
    val combinationAnalysis: CombinationAnalysis,
    val hanja1Info: HanjaInfo,
    val hanja2Info: HanjaInfo,
    val filteringProcess: MutableList<FilteringStep>,
    var combinedElement: String? = null,
    var combinedPm: String? = null,
    var combinedHanja: String? = null,
    var combinedPronounciation: String? = null
)

data class BirthInfo(
    val year: Int, 
    val month: Int, 
    val day: Int,
    val hour: Int, 
    val minute: Int
)

data class NameScore(
    val fourTypesLuck: Int,
    val nameElementHarmony: Int,
    val typeElementHarmony: Int,
    val yinYangBalance: Int,
    val sajuComplement: Int,
    val pronunciation: Int,
    val meaning: Int,
    val total: Int
)

data class NameExplanation(
    val summary: String,
    val sajuAnalysis: String,
    val strokeAnalysis: String,
    val elementHarmony: String,
    val yinYangBalance: String,
    val pronunciationAnalysis: String,
    val overallEvaluation: String,
    val recommendations: List<String>
)
