// model/data/GeneratedName.kt
package com.ssc.namespring.model.data

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

// has-a
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
// has-a
data class SajuAnalysisInfo(
    val fourPillars: Array<String>,
    val sajuOhaengCount: Map<String, Int>,
    val missingElements: List<String>,
    val dominantElements: List<String>,
    val elementBalance: Map<String, Float>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SajuAnalysisInfo

        if (!fourPillars.contentEquals(other.fourPillars)) return false
        if (sajuOhaengCount != other.sajuOhaengCount) return false
        if (missingElements != other.missingElements) return false
        if (dominantElements != other.dominantElements) return false
        if (elementBalance != other.elementBalance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fourPillars.contentHashCode()
        result = 31 * result + sajuOhaengCount.hashCode()
        result = 31 * result + missingElements.hashCode()
        result = 31 * result + dominantElements.hashCode()
        result = 31 * result + elementBalance.hashCode()
        return result
    }
}

data class YinYangAnalysisInfo(
    val combinedEumyang: String,
    val yinCount: Int,
    val yangCount: Int,
    val balance: Float,
    val pattern: String,
    val isBalanced: Boolean,
    val balanceDescription: String
)

data class OhaengAnalysisInfo(
    val baleumOhaeng: String,
    val hoeksuOhaeng: List<Int>,
    val jawonOhaeng: List<String>,
    val sagyeokSuriOhaeng: List<Int>,
    val harmonyScore: Int,
    val conflictingPairs: List<Pair<String, String>>,
    val generatingPairs: List<Pair<String, String>>,
    val overallHarmony: String
)

data class FilteringStep(
    val filterName: String,
    val passed: Boolean,
    val reason: String,
    val details: Map<String, Any>
)

// ..
// 더있다 치자.
// 외부 개발자가 볼땐 이거 다 분석해야됨. 빡침;;