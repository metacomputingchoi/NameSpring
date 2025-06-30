// model/filter/validation/strategies/impl/SingleCharNameStrategy.kt
package com.ssc.namingengine.filter.validation.strategies.impl

import com.ssc.namingengine.data.analysis.ValidationResult
import com.ssc.namingengine.filter.constants.ValidationConstants
import com.ssc.namingengine.filter.templates.JawonOhaengValidationTemplate
import com.ssc.namingengine.filter.validation.rules.eumyang.EumYangValidator
import com.ssc.namingengine.filter.validation.rules.ohaeng.SingleElementRule
import com.ssc.namingengine.filter.validation.strategies.AbstractNameLengthStrategy

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