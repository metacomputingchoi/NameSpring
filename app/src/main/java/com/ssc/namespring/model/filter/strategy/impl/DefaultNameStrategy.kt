// model/filter/strategy/impl/DefaultNameStrategy.kt
package com.ssc.namespring.model.filter.strategy.impl

import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.strategy.NameLengthStrategy
import com.ssc.namespring.model.util.ValidationResultFactory

class DefaultNameStrategy : NameLengthStrategy {

    override fun validateYinYang(
        eumyangList: List<Int>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        return ValidationResultFactory.createSuccess(
            "기타 구조",
            *details.toList().toTypedArray()
        )
    }

    override fun validateJawonOhaeng(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        return ValidationResultFactory.createSuccess(
            "기타 구조",
            *details.toList().toTypedArray()
        )
    }
}