// filter/validation/rules/ohaeng/AllSameElementRule.kt
package com.ssc.namingengine.filter.validation.rules.ohaeng

import com.ssc.namingengine.data.analysis.ValidationResult
import com.ssc.namingengine.filter.validation.ValidationResultBuilder

class AllSameElementRule : OhaengValidationRule {

    override fun validate(
        jawonElements: List<String>,
        targetElements: List<String>,
        elementType: String,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val targetElement = targetElements.first()
        val allSame = jawonElements.all { it == targetElement }
        details["모두동일오행"] = allSame

        return ValidationResultBuilder()
            .valid(allSame)
            .reason(
                if (allSame) "$elementType ${targetElement}으로 통일"
                else "${elementType}으로 통일되지 않음"
            )
            .details(details)
            .build()
    }
}