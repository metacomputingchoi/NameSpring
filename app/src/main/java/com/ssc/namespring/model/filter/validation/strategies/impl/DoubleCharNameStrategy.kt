// model/filter/validation/strategies/impl/DoubleCharNameStrategy.kt
package com.ssc.namespring.model.filter.validation.strategies.impl

import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.constants.ValidationConstants
import com.ssc.namespring.model.filter.templates.JawonOhaengValidationTemplate
import com.ssc.namespring.model.filter.validation.rules.eumyang.EumYangValidator
import com.ssc.namespring.model.filter.validation.rules.ohaeng.AllSameElementRule
import com.ssc.namespring.model.filter.validation.rules.ohaeng.MultipleElementRule
import com.ssc.namespring.model.filter.validation.strategies.AbstractNameLengthStrategy

class DoubleCharNameStrategy : AbstractNameLengthStrategy() {

    private val eumYangValidator = EumYangValidator()
    private val jawonTemplate = DoubleCharJawonTemplate()

    override fun validateEumYang(
        eumyangList: List<Int>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        // 1자성 2자이름인 경우
        if (eumyangList.size == 3) {
            val firstLastDifferent = eumYangValidator.validateFirstLastDifference(eumyangList)
            return createValidationResult(
                firstLastDifferent,
                if (firstLastDifferent) ValidationConstants.Messages.FIRST_LAST_DIFFERENT
                else ValidationConstants.Messages.FIRST_LAST_SAME,
                details
            )
        }

        // 음양 균형 체크
        val balance = eumYangValidator.validateBalance(eumyangList)
        if (!balance.isBalanced) {
            return createValidationResult(
                false,
                ValidationConstants.Messages.EUM_YANG_UNBALANCED,
                details
            )
        }

        // 연속 체크
        val maxConsecutive = if (eumyangList.size == 5) {
            ValidationConstants.EumYang.MAX_CONSECUTIVE_DOUBLE
        } else {
            ValidationConstants.EumYang.MAX_CONSECUTIVE_SINGLE
        }

        val consecutive = eumYangValidator.validateConsecutive(eumyangList, maxConsecutive)
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

    private class DoubleCharJawonTemplate : JawonOhaengValidationTemplate() {
        private val allSameRule = AllSameElementRule()
        private val multipleRule = MultipleElementRule(
            requireDifferent = true,
            expectedUniqueCount = ValidationConstants.JawonOhaeng.DoubleChar.ELEMENT_COUNT
        )

        override fun validateForZeroElements(
            jawonElements: List<String>,
            zeroElements: List<String>,
            details: MutableMap<String, Any>
        ): ValidationResult {
            return if (zeroElements.size == 1) {
                applyRule(allSameRule, jawonElements, zeroElements, "부족한 오행", details)
            } else {
                applyRule(multipleRule, jawonElements, zeroElements, "부족한 오행", details)
            }
        }

        override fun validateForOneElements(
            jawonElements: List<String>,
            oneElements: List<String>,
            details: MutableMap<String, Any>
        ): ValidationResult {
            return if (oneElements.size == 1) {
                checkElementInclusion(jawonElements, oneElements, "약한 오행", details)
            } else {
                applyRule(multipleRule, jawonElements, oneElements, "약한 오행", details)
            }
        }
    }
}