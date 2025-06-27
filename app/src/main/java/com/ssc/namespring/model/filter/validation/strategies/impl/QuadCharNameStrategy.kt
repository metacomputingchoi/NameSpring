// model/filter/validation/strategies/impl/QuadCharNameStrategy.kt
package com.ssc.namespring.model.filter.validation.strategies.impl

import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.constants.ValidationConstants
import com.ssc.namespring.model.filter.templates.JawonOhaengValidationTemplate
import com.ssc.namespring.model.filter.validation.rules.eumyang.EumYangValidator
import com.ssc.namespring.model.filter.validation.rules.ohaeng.AllSameElementRule
import com.ssc.namespring.model.filter.validation.rules.ohaeng.BalancedElementRule
import com.ssc.namespring.model.filter.validation.rules.ohaeng.MultipleElementRule
import com.ssc.namespring.model.filter.validation.strategies.AbstractNameLengthStrategy

class QuadCharNameStrategy : AbstractNameLengthStrategy() {

    private val eumYangValidator = EumYangValidator()
    private val jawonTemplate = QuadCharJawonTemplate()

    override fun validateEumYang(
        eumyangList: List<Int>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val balance = eumYangValidator.validateBalance(eumyangList)

        if (!balance.isBalanced) {
            return createValidationResult(
                false,
                ValidationConstants.Messages.EUM_YANG_UNBALANCED,
                details
            )
        }

        val consecutive = eumYangValidator.validateConsecutive(
            eumyangList,
            ValidationConstants.EumYang.MAX_CONSECUTIVE_TRIPLE
        )

        return createValidationResult(
            consecutive,
            if (consecutive) ValidationConstants.Messages.EUM_YANG_BALANCED
            else ValidationConstants.Messages.EUM_YANG_CONSECUTIVE,
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
        return jawonTemplate.validate(jawonElements, zeroElements, oneElements, details)
    }

    private class QuadCharJawonTemplate : JawonOhaengValidationTemplate() {
        private val allSameRule = AllSameElementRule()
        private val pairRule = BalancedElementRule(
            expectedCount = ValidationConstants.JawonOhaeng.PAIR_COUNT
        )
        private val tripleRule = BalancedElementRule() // 2-1-1 구성은 내부에서 처리
        private val multipleRule = MultipleElementRule(
            requireDifferent = true,
            expectedUniqueCount = ValidationConstants.JawonOhaeng.QuadChar.EXPECTED_UNIQUE_COUNT
        )

        override fun validateForZeroElements(
            jawonElements: List<String>,
            zeroElements: List<String>,
            details: MutableMap<String, Any>
        ): ValidationResult {
            return when (zeroElements.size) {
                1 -> applyRule(allSameRule, jawonElements, zeroElements, "부족한 오행", details)
                2 -> applyRule(pairRule, jawonElements, zeroElements, "부족 오행", details)
                3 -> applyRule(tripleRule, jawonElements, zeroElements, "부족 오행", details)
                else -> applyRule(multipleRule, jawonElements, zeroElements, "부족 오행", details)
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