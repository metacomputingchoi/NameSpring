// model/filter/AbstractNameFilter.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.FilteringStep
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.exception.NamingException

abstract class AbstractNameFilter : NameFilterStrategy {

    override fun filter(names: List<GeneratedName>, context: FilterContext): List<GeneratedName> {
        return names.filter { name ->
            try {
                isValid(name, context)
            } catch (e: Exception) {
                throw NamingException.FilteringException(
                    "${getFilterName()} 필터 처리 중 오류 발생",
                    filterName = getFilterName(),
                    cause = e
                )
            }
        }
    }

    override fun filterBatch(names: Sequence<GeneratedName>, context: FilterContext): Sequence<GeneratedName> {
        return names.filter { name ->
            try {
                isValid(name, context)
            } catch (e: Exception) {
                throw NamingException.FilteringException(
                    "${getFilterName()} 필터 배치 처리 중 오류 발생",
                    filterName = getFilterName(),
                    cause = e
                )
            }
        }
    }

    override fun evaluate(name: GeneratedName, context: FilterContext): FilteringStep {
        return try {
            val validationResult = getValidationDetails(name, context)

            FilteringStep(
                filterName = getFilterName(),
                passed = validationResult.isValid,
                reason = validationResult.reason,
                details = validationResult.details
            )
        } catch (e: Exception) {
            FilteringStep(
                filterName = getFilterName(),
                passed = false,
                reason = "평가 중 오류 발생: ${e.message}",
                details = emptyMap()
            )
        }
    }

    protected abstract fun getFilterName(): String
    protected abstract fun isValid(name: GeneratedName, context: FilterContext): Boolean
    protected abstract fun getValidationDetails(name: GeneratedName, context: FilterContext): ValidationResult
}