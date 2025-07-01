// data/ValidationResult.kt
package com.ssc.namingengine.data

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)