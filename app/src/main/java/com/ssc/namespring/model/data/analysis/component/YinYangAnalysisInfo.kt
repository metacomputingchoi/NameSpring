// model/data/analysis/component/YinYangAnalysisInfo.kt
package com.ssc.namespring.model.data.analysis.component

data class YinYangAnalysisInfo(
    val combinedEumyang: String,
    val yinCount: Int,
    val yangCount: Int,
    val balance: Float,
    val pattern: String,
    val isBalanced: Boolean,
    val balanceDescription: String
)