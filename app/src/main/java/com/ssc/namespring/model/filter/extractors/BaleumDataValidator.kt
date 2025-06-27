// model/filter/extractors/BaleumDataValidator.kt
package com.ssc.namespring.model.filter.extractors

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.validation.ValidationResultBuilder
import com.ssc.namespring.model.service.EumYangAnalysisService
import com.ssc.namespring.model.util.ValidationResultFactory

class BaleumDataValidator(
    private val eumYangAnalysisService: EumYangAnalysisService,
    private val checkBaleumOhaengHarmony: (String) -> Boolean
) {

    fun validate(
        baleumData: BaleumData,
        context: FilterContext,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val totalChars = context.surLength + context.nameLength

        if (!isDataComplete(baleumData, totalChars)) {
            return ValidationResultFactory.createFailure(
                "발음오행 또는 음양 정보가 누락됨",
                *details.toList().toTypedArray()
            )
        }

        val eumYangResult = checkEumYangBalance(baleumData.combinedEumyang, context, details)
        if (!eumYangResult.isValid) {
            return eumYangResult
        }

        return checkOhaengHarmony(baleumData.combinedBaleumOhaeng, details)
    }

    private fun isDataComplete(baleumData: BaleumData, totalChars: Int): Boolean {
        return baleumData.combinedBaleumOhaeng.length == totalChars &&
                baleumData.combinedEumyang.length == totalChars
    }

    private fun checkEumYangBalance(
        combinedEumyang: String,
        context: FilterContext,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val eumYangResult = eumYangAnalysisService.checkEumYangBalanceWithDetails(
            combinedEumyang,
            context.surLength,
            context.nameLength
        )

        details["음양균형"] = eumYangResult.details

        return if (eumYangResult.isValid) {
            ValidationResultBuilder()
                .valid(true)
                .reason(eumYangResult.reason)
                .details(details)
                .build()
        } else {
            ValidationResultBuilder()
                .valid(false)
                .reason(eumYangResult.reason)
                .details(details)
                .build()
        }
    }

    private fun checkOhaengHarmony(
        combinedBaleumOhaeng: String,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val harmonyValid = checkBaleumOhaengHarmony(combinedBaleumOhaeng)
        details["오행조화"] = if (harmonyValid) "조화로움" else "부조화"

        return ValidationResultFactory.createConditional(
            harmonyValid,
            "발음오행이 조화로움",
            "발음오행이 상극 관계",
            *details.toList().toTypedArray()
        )
    }
}