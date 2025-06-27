// model/filter/validation/rules/ohaeng/OhaengValidationRule.kt
package com.ssc.namespring.model.filter.validation.rules.ohaeng

import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.validation.rules.ValidationRule

data class OhaengValidationData(
    val jawonElements: List<String>,
    val targetElements: List<String>,
    val elementType: String
)

interface OhaengValidationRule : ValidationRule<OhaengValidationData> {
    override fun validate(data: OhaengValidationData, details: MutableMap<String, Any>): ValidationResult {
        return validate(data.jawonElements, data.targetElements, data.elementType, details)
    }

    fun validate(
        jawonElements: List<String>,
        targetElements: List<String>,
        elementType: String,
        details: MutableMap<String, Any>
    ): ValidationResult
}