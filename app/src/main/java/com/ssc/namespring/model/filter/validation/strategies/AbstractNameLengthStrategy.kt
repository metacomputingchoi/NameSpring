// model/filter/validation/strategies/AbstractNameLengthStrategy.kt
package com.ssc.namespring.model.filter.validation.strategies

import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.validation.ValidationResultBuilder

abstract class AbstractNameLengthStrategy : NameLengthStrategy {

    protected fun createValidationResult(
        isValid: Boolean,
        reason: String,
        details: MutableMap<String, Any>
    ): ValidationResult {
        return ValidationResultBuilder()
            .valid(isValid)
            .reason(reason)
            .details(details)
            .build()
    }

    protected fun addElementComposition(
        elements: List<String>,
        details: MutableMap<String, Any>
    ) {
        details["자원오행구성"] = elements.groupingBy { it }.eachCount()
    }
}