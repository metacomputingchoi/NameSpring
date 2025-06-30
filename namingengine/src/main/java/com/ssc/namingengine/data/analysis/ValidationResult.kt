// model/data/analysis/ValidationResult.kt
package com.ssc.namingengine.data.analysis

data class ValidationResult(
    val isValid: Boolean,
    val reason: String,
    val details: Map<String, Any>
)