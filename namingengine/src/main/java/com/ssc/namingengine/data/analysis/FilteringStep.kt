// model/data/analysis/FilteringStep.kt
package com.ssc.namingengine.data.analysis

data class FilteringStep(
    val filterName: String,
    val passed: Boolean,
    val reason: String,
    val details: Map<String, Any>
)
