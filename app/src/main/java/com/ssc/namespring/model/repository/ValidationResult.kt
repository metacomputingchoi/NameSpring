// model/repository/ValidationResult.kt
package com.ssc.namespring.model.repository

data class ValidationResult(
    val isValid: Boolean,
    val warnings: List<String>,
    val criticalErrors: List<String>
)