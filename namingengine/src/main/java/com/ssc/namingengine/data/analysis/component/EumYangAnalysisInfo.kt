// data/analysis/component/EumYangAnalysisInfo.kt
package com.ssc.namingengine.data.analysis.component

data class EumYangAnalysisInfo(
    val combinedEumyang: String,
    val eumCount: Int,
    val yangCount: Int,
    val balance: Float,
    val pattern: String,
    val isBalanced: Boolean,
    val balanceDescription: String
)
