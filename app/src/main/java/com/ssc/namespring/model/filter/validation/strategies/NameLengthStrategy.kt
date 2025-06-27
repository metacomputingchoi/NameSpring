// model/filter/validation/strategies/NameLengthStrategy.kt
package com.ssc.namespring.model.filter.validation.strategies

import com.ssc.namespring.model.data.analysis.ValidationResult

interface NameLengthStrategy {
    fun validateEumYang(eumyangList: List<Int>, details: MutableMap<String, Any>): ValidationResult
    fun validateJawonOhaeng(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>,
        details: MutableMap<String, Any>
    ): ValidationResult
}