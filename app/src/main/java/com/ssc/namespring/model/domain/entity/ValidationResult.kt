// model/domain/entity/ValidationResult.kt
package com.ssc.namespring.model.domain.entity

data class ValidationResult(
    val isValid: Boolean,
    val warnings: List<String>,
    val criticalErrors: List<String>
)