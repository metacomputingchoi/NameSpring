// model/filter/validation/rules/ohaeng/SingleElementRule.kt
package com.ssc.namespring.model.filter.validation.rules.ohaeng

import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.validation.ValidationResultBuilder

class SingleElementRule : OhaengValidationRule {

    override fun validate(
        jawonElements: List<String>,
        targetElements: List<String>,
        elementType: String,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val element = jawonElements.firstOrNull()
            ?: return ValidationResultBuilder()
                .valid(false)
                .reason("자원 오행이 없음")
                .details(details)
                .build()

        val action = if (elementType.contains("부족")) "보완" else "보강"
        val contains = element in targetElements
        details["${action}오행포함"] = contains

        return ValidationResultBuilder()
            .valid(contains)
            .reason(
                if (contains) "$elementType $element 을(를) ${action}함"
                else "${elementType}을(를) ${action}하지 못함"
            )
            .details(details)
            .build()
    }
}