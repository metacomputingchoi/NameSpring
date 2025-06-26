// model/util/ValidationResultFactory.kt
package com.ssc.namespring.model.util

import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.FilterValidationHelper

object ValidationResultFactory {

    fun createSuccess(
        reason: String,
        vararg details: Pair<String, Any>
    ): ValidationResult {
        return ValidationResult(
            isValid = true,
            reason = reason,
            details = FilterValidationHelper.createDetails(*details)
        )
    }

    fun createFailure(
        reason: String,
        vararg details: Pair<String, Any>
    ): ValidationResult {
        return ValidationResult(
            isValid = false,
            reason = reason,
            details = FilterValidationHelper.createDetails(*details)
        )
    }

    fun createConditional(
        condition: Boolean,
        successReason: String,
        failureReason: String,
        vararg details: Pair<String, Any>
    ): ValidationResult {
        return ValidationResult(
            isValid = condition,
            reason = if (condition) successReason else failureReason,
            details = FilterValidationHelper.createDetails(*details)
        )
    }
}