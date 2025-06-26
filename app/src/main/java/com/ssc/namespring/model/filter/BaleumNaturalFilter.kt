// model/filter/BaleumNaturalFilter.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.ValidationResult

class BaleumNaturalFilter(
    private val dictProvider: () -> Set<String>
) : AbstractNameFilter() {

    override fun getFilterName(): String = "발음자연스러움필터"

    override fun isValid(name: GeneratedName, context: FilterContext): Boolean {
        val fullName = name.surnameHangul + name.combinedPronounciation
        return if (fullName.length > context.surHangul.length) {
            val namePart = fullName.substring(context.surHangul.length)
            namePart.length != 2 || namePart in dictProvider()
        } else {
            true
        }
    }

    override fun getValidationDetails(name: GeneratedName, context: FilterContext): ValidationResult {
        val fullName = name.surnameHangul + name.combinedPronounciation
        val namePart = if (fullName.length > context.surHangul.length) {
            fullName.substring(context.surHangul.length)
        } else {
            ""
        }

        val details = mutableMapOf<String, Any>(
            "전체이름" to fullName,
            "이름부분" to namePart,
            "이름길이" to namePart.length
        )

        val (isValid, reason) = when {
            namePart.isEmpty() -> {
                true to "이름 부분이 없음"
            }
            namePart.length != 2 -> {
                true to "2글자 이름이 아니므로 자연스러움 체크 제외"
            }
            namePart in dictProvider() -> {
                details["사전등재여부"] = true
                true to "자연스러운 한글 이름 (사전 등재)"
            }
            else -> {
                details["사전등재여부"] = false
                false to "사전에 없는 2글자 조합"
            }
        }

        return ValidationResult(isValid, reason, details)
    }
}