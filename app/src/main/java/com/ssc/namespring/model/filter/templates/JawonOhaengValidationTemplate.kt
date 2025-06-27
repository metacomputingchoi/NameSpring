// model/filter/templates/JawonOhaengValidationTemplate.kt
package com.ssc.namespring.model.filter.templates

import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.constants.ValidationConstants
import com.ssc.namespring.model.filter.validation.ValidationResultBuilder
import com.ssc.namespring.model.filter.validation.rules.ohaeng.OhaengValidationRule

abstract class JawonOhaengValidationTemplate {

    fun validate(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>,
        details: MutableMap<String, Any>
    ): ValidationResult {

        return when {
            zeroElements.isNotEmpty() -> validateForZeroElements(
                jawonElements, zeroElements, details
            )
            oneElements.isNotEmpty() -> validateForOneElements(
                jawonElements, oneElements, details
            )
            else -> createBalancedResult(details)
        }
    }

    protected abstract fun validateForZeroElements(
        jawonElements: List<String>,
        zeroElements: List<String>,
        details: MutableMap<String, Any>
    ): ValidationResult

    protected abstract fun validateForOneElements(
        jawonElements: List<String>,
        oneElements: List<String>,
        details: MutableMap<String, Any>
    ): ValidationResult

    protected fun createBalancedResult(details: MutableMap<String, Any>): ValidationResult {
        return ValidationResultBuilder()
            .valid(true)
            .reason(ValidationConstants.Messages.BALANCED_SAJU)
            .details(details)
            .build()
    }

    protected fun applyRule(
        rule: OhaengValidationRule,
        jawonElements: List<String>,
        targetElements: List<String>,
        elementType: String,
        details: MutableMap<String, Any>
    ): ValidationResult {
        return rule.validate(jawonElements, targetElements, elementType, details)
    }

    protected fun checkElementInclusion(
        elements: List<String>,
        targetElements: List<String>,
        elementType: String,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val contains = elements.any { it in targetElements }
        details["${elementType}포함"] = contains

        return ValidationResultBuilder.conditional(
            contains,
            "${elementType}을(를) 포함",
            "${elementType}을(를) 포함하지 않음",
            *details.toList().toTypedArray()
        )
    }
}