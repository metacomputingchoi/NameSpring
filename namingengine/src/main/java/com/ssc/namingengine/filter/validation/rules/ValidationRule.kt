// filter/validation/rules/ValidationRule.kt
package com.ssc.namingengine.filter.validation.rules

import com.ssc.namingengine.data.analysis.ValidationResult

interface ValidationRule<T> {
    fun validate(data: T, details: MutableMap<String, Any>): ValidationResult
}