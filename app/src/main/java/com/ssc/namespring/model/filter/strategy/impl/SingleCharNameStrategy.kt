// model/filter/strategy/impl/SingleCharNameStrategy.kt
package com.ssc.namespring.model.filter.strategy.impl

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.strategy.NameLengthStrategy
import com.ssc.namespring.model.util.ValidationResultFactory

class SingleCharNameStrategy : NameLengthStrategy {

    override fun validateYinYang(
        eumyangList: List<Int>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val eumyang = eumyangList.joinToString("")
        val firstLast = eumyang.first() != eumyang.last()

        return ValidationResultFactory.createConditional(
            firstLast,
            "처음과 끝의 음양이 다름",
            "처음과 끝의 음양이 같음",
            *details.toList().toTypedArray()
        )
    }

    override fun validateJawonOhaeng(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        return when {
            zeroElements.isNotEmpty() -> {
                val contains = jawonElements[0] in zeroElements
                details["보완오행포함"] = contains
                ValidationResultFactory.createConditional(
                    contains,
                    "부족한 오행 ${jawonElements[0]}을(를) 보완함",
                    "부족한 오행을 보완하지 못함",
                    *details.toList().toTypedArray()
                )
            }
            oneElements.isNotEmpty() -> {
                val contains = jawonElements[0] in oneElements
                details["보강오행포함"] = contains
                ValidationResultFactory.createConditional(
                    contains,
                    "약한 오행 ${jawonElements[0]}을(를) 보강함",
                    "약한 오행을 보강하지 못함",
                    *details.toList().toTypedArray()
                )
            }
            else -> ValidationResultFactory.createSuccess(
                "사주 오행이 균형잡혀 있음",
                *details.toList().toTypedArray()
            )
        }
    }
}