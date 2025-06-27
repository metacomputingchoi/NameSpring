// model/filter/validation/strategies/impl/TripleCharNameStrategy.kt
package com.ssc.namespring.model.filter.validation.strategies.impl

import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.constants.ValidationConstants
import com.ssc.namespring.model.filter.templates.JawonOhaengValidationTemplate
import com.ssc.namespring.model.filter.validation.rules.eumyang.EumYangValidator
import com.ssc.namespring.model.filter.validation.rules.ohaeng.AllSameElementRule
import com.ssc.namespring.model.filter.validation.rules.ohaeng.BalancedElementRule
import com.ssc.namespring.model.filter.validation.rules.ohaeng.MultipleElementRule
import com.ssc.namespring.model.filter.validation.strategies.AbstractNameLengthStrategy
import kotlin.math.abs

class TripleCharNameStrategy : AbstractNameLengthStrategy() {

    private val eumYangValidator = EumYangValidator()
    private val jawonTemplate = TripleCharJawonTemplate()

    override fun validateEumYang(
        eumyangList: List<Int>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        return if (eumyangList.size == 4) {
            // 1자성 3자이름
            val consecutive = eumYangValidator.validateConsecutive(
                eumyangList,
                ValidationConstants.EumYang.MAX_CONSECUTIVE_SINGLE
            )
            createValidationResult(
                consecutive,
                if (consecutive) "연속 음양 제한 통과"
                else ValidationConstants.Messages.EUM_YANG_CONSECUTIVE,
                details
            )
        } else {
            // 2자성 3자이름
            val balance = eumYangValidator.validateBalance(eumyangList)
            val diff = abs(balance.eumCount - balance.yangCount)

            if (diff != 1) {
                return createValidationResult(
                    false,
                    "음양 개수 차이가 1이 아님",
                    details
                )
            }

            val consecutive = eumYangValidator.validateConsecutive(
                eumyangList,
                ValidationConstants.EumYang.MAX_CONSECUTIVE_DOUBLE
            )

            createValidationResult(
                consecutive,
                if (consecutive) ValidationConstants.Messages.EUM_YANG_BALANCED
                else ValidationConstants.Messages.EUM_YANG_CONSECUTIVE,
                details
            )
        }
    }

    override fun validateJawonOhaeng(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        addElementComposition(jawonElements, details)
        return jawonTemplate.validate(jawonElements, zeroElements, oneElements, details)
    }

    private class TripleCharJawonTemplate : JawonOhaengValidationTemplate() {
        private val allSameRule = AllSameElementRule()
        private val balancedRule = BalancedElementRule(
            minCountPerElement = ValidationConstants.JawonOhaeng.TripleChar.MIN_COUNT_PER_ELEMENT
        )
        private val multipleRule = MultipleElementRule(
            requireDifferent = true,
            expectedUniqueCount = ValidationConstants.JawonOhaeng.TripleChar.ELEMENT_COUNT
        )

        override fun validateForZeroElements(
            jawonElements: List<String>,
            zeroElements: List<String>,
            details: MutableMap<String, Any>
        ): ValidationResult {
            return when (zeroElements.size) {
                1 -> applyRule(allSameRule, jawonElements, zeroElements, "부족한 오행", details)
                2 -> applyRule(balancedRule, jawonElements, zeroElements, "부족한 오행", details)
                else -> applyRule(multipleRule, jawonElements, zeroElements, "부족한 오행", details)
            }
        }

        override fun validateForOneElements(
            jawonElements: List<String>,
            oneElements: List<String>,
            details: MutableMap<String, Any>
        ): ValidationResult {
            return checkElementInclusion(jawonElements, oneElements, "약한 오행", details)
        }
    }
}