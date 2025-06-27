// model/filter/filters/BaleumNaturalFilter.kt
package com.ssc.namespring.model.filter.filters

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.constants.FilterConstants
import com.ssc.namespring.model.filter.core.AbstractNameFilter
import com.ssc.namespring.model.filter.utils.FilterValidationHelper
import com.ssc.namespring.model.filter.validation.ValidationResultBuilder

class BaleumNaturalFilter(
    private val dictProvider: () -> Set<String>
) : AbstractNameFilter() {

    override fun getName(): String = FilterConstants.BALEUM_NATURAL_FILTER

    override fun getValidationDetails(name: GeneratedName, context: FilterContext): ValidationResult {
        val namePart = extractNamePart(name, context)
        val details = createDetailsMap(name, namePart)

        return validateNaturalness(namePart, details)
    }

    private fun extractNamePart(name: GeneratedName, context: FilterContext): String {
        val fullName = name.surnameHangul + name.combinedPronounciation
        return if (fullName.length > context.surHangul.length) {
            fullName.substring(context.surHangul.length)
        } else {
            ""
        }
    }

    private fun createDetailsMap(name: GeneratedName, namePart: String): MutableMap<String, Any> {
        val fullName = name.surnameHangul + name.combinedPronounciation
        return FilterValidationHelper.createDetails(
            "전체이름" to fullName,
            "이름부분" to namePart,
            "이름길이" to namePart.length
        )
    }

    private fun validateNaturalness(
        namePart: String,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val validationResultBuilder = ValidationResultBuilder()
            .details(details)

        return when {
            namePart.isEmpty() -> {
                validationResultBuilder
                    .valid(true)
                    .reason("이름 부분이 없음")
                    .build()
            }
            namePart.length != 2 -> {
                validationResultBuilder
                    .valid(true)
                    .reason("2글자 이름이 아니므로 자연스러움 체크 제외")
                    .build()
            }
            namePart in dictProvider() -> {
                details["사전등재여부"] = true
                validationResultBuilder
                    .valid(true)
                    .reason("자연스러운 한글 이름 (사전 등재)")
                    .build()
            }
            else -> {
                details["사전등재여부"] = false
                validationResultBuilder
                    .valid(false)
                    .reason("사전에 없는 2글자 조합")
                    .build()
            }
        }
    }
}