// model/filter/validation/ValidationResultBuilder.kt
package com.ssc.namespring.model.filter.validation

import com.ssc.namespring.model.data.analysis.ValidationResult

class ValidationResultBuilder {
    private var isValid: Boolean = true
    private var reason: String = ""
    private val details = mutableMapOf<String, Any>()

    fun valid(isValid: Boolean) = apply { this.isValid = isValid }

    fun reason(reason: String) = apply { this.reason = reason }

    fun detail(key: String, value: Any) = apply { details[key] = value }

    fun details(vararg pairs: Pair<String, Any>) = apply {
        details.putAll(pairs.toMap())
    }

    fun details(map: Map<String, Any>) = apply {
        details.putAll(map)
    }

    fun build(): ValidationResult = ValidationResult(isValid, reason, details)

    companion object {
        fun success(reason: String, vararg detailPairs: Pair<String, Any>): ValidationResult {
            return ValidationResultBuilder()
                .valid(true)
                .reason(reason)
                .details(*detailPairs)
                .build()
        }

        fun failure(reason: String, vararg detailPairs: Pair<String, Any>): ValidationResult {
            return ValidationResultBuilder()
                .valid(false)
                .reason(reason)
                .details(*detailPairs)
                .build()
        }

        fun conditional(
            condition: Boolean,
            successReason: String,
            failureReason: String,
            vararg detailPairs: Pair<String, Any>
        ): ValidationResult {
            return ValidationResultBuilder()
                .valid(condition)
                .reason(if (condition) successReason else failureReason)
                .details(*detailPairs)
                .build()
        }
    }
}