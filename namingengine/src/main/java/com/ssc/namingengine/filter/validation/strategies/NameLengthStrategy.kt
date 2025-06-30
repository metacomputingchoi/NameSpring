// model/filter/validation/strategies/NameLengthStrategy.kt
package com.ssc.namingengine.filter.validation.strategies

import com.ssc.namingengine.data.analysis.ValidationResult

interface NameLengthStrategy {
    fun validateEumYang(eumyangList: List<Int>, details: MutableMap<String, Any>): ValidationResult
    fun validateJawonOhaeng(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>,
        details: MutableMap<String, Any>
    ): ValidationResult
}