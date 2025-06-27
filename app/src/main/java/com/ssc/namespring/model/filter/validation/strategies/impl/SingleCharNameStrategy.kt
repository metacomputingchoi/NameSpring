// model/filter/validation/strategies/impl/SingleCharNameStrategy.kt
package com.ssc.namespring.model.filter.validation.strategies.impl

import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.constants.ValidationConstants
import com.ssc.namespring.model.filter.templates.JawonOhaengValidationTemplate
import com.ssc.namespring.model.filter.validation.rules.eumyang.EumYangValidator
import com.ssc.namespring.model.filter.validation.rules.ohaeng.SingleElementRule
import com.ssc.namespring.model.filter.validation.strategies.AbstractNameLengthStrategy

class SingleCharNameStrategy : AbstractNameLengthStrategy() {

    private val eumYangValidator = EumYangValidator()
    private val jawonTemplate = SingleCharJawonTemplate()

    override fun validateEumYang(
        eumyangList: List<Int>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val firstLastDifferent = eumYangValidator.validateFirstLastDifference(eumyangList)

        return createValidationResult(
            firstLastDifferent,
            if (firstLastDifferent) ValidationConstants.Messages.FIRST_LAST_DIFFERENT
            else ValidationConstants.Messages.FIRST_LAST_SAME,
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

    private class SingleCharJawonTemplate : JawonOhaengValidationTemplate() {
        private val singleRule = SingleElementRule()

        override fun validateForZeroElements(
            jawonElements: List<String>,
            zeroElements: List<String>,
            details: MutableMap<String, Any>
        ): ValidationResult {
            return applyRule(singleRule, jawonElements, zeroElements, "부족한 오행", details)
        }

        override fun validateForOneElements(
            jawonElements: List<String>,
            oneElements: List<String>,
            details: MutableMap<String, Any>
        ): ValidationResult {
            return applyRule(singleRule, jawonElements, oneElements, "약한 오행", details)
        }
    }
}