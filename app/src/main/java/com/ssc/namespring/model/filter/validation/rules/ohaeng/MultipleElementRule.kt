// model/filter/validation/rules/ohaeng/MultipleElementRule.kt
package com.ssc.namespring.model.filter.validation.rules.ohaeng

import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.validation.ValidationResultBuilder

class MultipleElementRule(
    private val requireDifferent: Boolean = true,
    private val expectedUniqueCount: Int? = null
) : OhaengValidationRule {

    override fun validate(
        jawonElements: List<String>,
        targetElements: List<String>,
        elementType: String,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val allInTarget = jawonElements.all { it in targetElements }
        val uniqueCount = jawonElements.toSet().size
        val hasDifferent = uniqueCount > 1

        details["모두${elementType}포함"] = allInTarget
        details["고유오행개수"] = uniqueCount

        if (!allInTarget) {
            return createFailure("${elementType}이 아닌 것이 포함됨", details)
        }

        if (requireDifferent) {
            details["서로다른오행"] = hasDifferent
            if (!hasDifferent) {
                return createFailure("같은 오행으로 구성됨", details)
            }
        }

        if (expectedUniqueCount != null && uniqueCount != expectedUniqueCount) {
            return createFailure(
                "${expectedUniqueCount}개의 서로 다른 오행으로 구성되지 않음",
                details
            )
        }

        val reasonParts = mutableListOf<String>()
        if (requireDifferent) reasonParts.add("서로 다른")
        reasonParts.add(elementType)
        if (expectedUniqueCount != null) {
            reasonParts.add("${expectedUniqueCount}개")
        }
        reasonParts.add("구성")

        return createSuccess(reasonParts.joinToString(" "), details)
    }

    private fun createSuccess(reason: String, details: MutableMap<String, Any>): ValidationResult {
        return ValidationResultBuilder()
            .valid(true)
            .reason(reason)
            .details(details)
            .build()
    }

    private fun createFailure(reason: String, details: MutableMap<String, Any>): ValidationResult {
        return ValidationResultBuilder()
            .valid(false)
            .reason(reason)
            .details(details)
            .build()
    }
}