// model/filter/validation/rules/ValidationRule.kt
package com.ssc.namespring.model.filter.validation.rules

import com.ssc.namespring.model.data.analysis.ValidationResult

interface ValidationRule<T> {
    fun validate(data: T, details: MutableMap<String, Any>): ValidationResult
}