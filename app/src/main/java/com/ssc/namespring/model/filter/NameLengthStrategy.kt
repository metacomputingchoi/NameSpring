// model/filter/strategy/NameLengthStrategy.kt
package com.ssc.namespring.model.filter.strategy

import com.ssc.namespring.model.data.analysis.ValidationResult

interface NameLengthStrategy {
    fun validateYinYang(eumyangList: List<Int>, details: MutableMap<String, Any>): ValidationResult
    fun validateJawonOhaeng(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>,
        details: MutableMap<String, Any>
    ): ValidationResult
}