// model/data/analysis/component/SajuAnalysisInfo.kt
package com.ssc.namespring.model.data.analysis.component

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
