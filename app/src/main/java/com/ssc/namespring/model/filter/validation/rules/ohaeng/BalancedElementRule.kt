// model/filter/validation/rules/ohaeng/BalancedElementRule.kt
package com.ssc.namespring.model.filter.validation.rules.ohaeng

import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.constants.ValidationConstants
import com.ssc.namespring.model.filter.validation.ValidationResultBuilder

class BalancedElementRule(
    private val expectedCount: Int? = null,
    private val minCountPerElement: Int? = null
) : OhaengValidationRule {

    override fun validate(
        jawonElements: List<String>,
        targetElements: List<String>,
        elementType: String,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val counts = targetElements.associateWith { target ->
            jawonElements.count { it == target }
        }

        details["오행별개수"] = counts

        // 모든 원소가 특정 개수인지 체크
        if (expectedCount != null) {
            val allExpected = counts.values.all { it == expectedCount }
            if (!allExpected) {
                return ValidationResultBuilder()
                    .valid(false)
                    .reason("오행이 균형있게 구성되지 않음")
                    .details(details)
                    .build()
            }
        }

        // 최소 개수 체크
        if (minCountPerElement != null) {
            val allHaveMin = counts.values.all { it >= minCountPerElement }
            if (!allHaveMin) {
                return ValidationResultBuilder()
                    .valid(false)
                    .reason("각 오행이 최소 ${minCountPerElement}개씩 포함되지 않음")
                    .details(details)
                    .build()
            }
        }

        // 특수 케이스: 2-1-1 구성 체크
        if (jawonElements.size == ValidationConstants.JawonOhaeng.QuadChar.ELEMENT_COUNT &&
            targetElements.size == 3) {
            val hasPair = counts.values.contains(ValidationConstants.JawonOhaeng.PAIR_COUNT)
            val singleCount = counts.values.count { it == ValidationConstants.JawonOhaeng.SINGLE_COUNT }

            details["쌍포함"] = hasPair
            details["단일개수"] = singleCount

            return when {
                !hasPair -> ValidationResultBuilder()
                    .valid(false)
                    .reason("쌍으로 구성된 오행이 없음")
                    .details(details)
                    .build()
                singleCount != ValidationConstants.JawonOhaeng.QuadChar.EXPECTED_SINGLE_COUNT_FOR_TRIPLE ->
                    ValidationResultBuilder()
                        .valid(false)
                        .reason("단일 오행 개수가 맞지 않음")
                        .details(details)
                        .build()
                else -> ValidationResultBuilder()
                    .valid(true)
                    .reason("세 ${elementType}이 2-1-1로 구성")
                    .details(details)
                    .build()
            }
        }

        return ValidationResultBuilder()
            .valid(true)
            .reason("${elementType}을(를) 균형있게 포함")
            .details(details)
            .build()
    }
}