// model/data/analysis/FilteringStep.kt
package com.ssc.namespring.model.data.analysis

data class FilteringStep(
    val filterName: String,
    val passed: Boolean,
    val reason: String,
    val details: Map<String, Any>
)