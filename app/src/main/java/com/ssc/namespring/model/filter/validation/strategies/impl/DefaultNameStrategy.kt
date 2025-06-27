// model/filter/validation/strategies/impl/DefaultNameStrategy.kt
package com.ssc.namespring.model.filter.validation.strategies.impl

import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.validation.strategies.AbstractNameLengthStrategy

class DefaultNameStrategy : AbstractNameLengthStrategy() {

    override fun validateEumYang(
        eumyangList: List<Int>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        return createValidationResult(
            true,
            "기타 구조",
            details
        )
    }

    override fun validateJawonOhaeng(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        addElementComposition(jawonElements, details)
        return createValidationResult(
            true,
            "기타 구조",
            details
        )
    }
}