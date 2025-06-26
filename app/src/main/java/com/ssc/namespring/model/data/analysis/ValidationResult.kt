// model/data/analysis/ValidationResult.kt
package com.ssc.namespring.model.data.analysis

data class ValidationResult(
    val isValid: Boolean,
    val reason: String,
    val details: Map<String, Any>
)