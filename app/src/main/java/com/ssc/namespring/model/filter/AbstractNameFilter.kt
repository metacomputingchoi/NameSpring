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
                getValidationDetails(name, context).isValid
            } catch (e: Exception) {
                handleFilterError(e)
            }
        }
    }

    override fun filterBatch(names: Sequence<GeneratedName>, context: FilterContext): Sequence<GeneratedName> {
        return names.filter { name ->
            try {
                getValidationDetails(name, context).isValid
            } catch (e: Exception) {
                handleFilterError(e)
            }
        }
    }

    override fun evaluate(name: GeneratedName, context: FilterContext): FilteringStep {
        return try {
            val validationResult = getValidationDetails(name, context)
            createFilteringStep(validationResult)
        } catch (e: Exception) {
            createErrorFilteringStep(e)
        }
    }

    protected abstract fun getFilterName(): String
    protected abstract fun getValidationDetails(name: GeneratedName, context: FilterContext): ValidationResult

    protected fun createFilteringStep(validationResult: ValidationResult): FilteringStep {
        return FilteringStep(
            filterName = getFilterName(),
            passed = validationResult.isValid,
            reason = validationResult.reason,
            details = validationResult.details
        )
    }

    private fun createErrorFilteringStep(e: Exception): FilteringStep {
        return FilteringStep(
            filterName = getFilterName(),
            passed = false,
            reason = FilterConstants.EVALUATION_ERROR_TEMPLATE.format(e.message),
            details = emptyMap()
        )
    }

    private fun handleFilterError(e: Exception): Boolean {
        throw NamingException.FilteringException(
            FilterConstants.FILTER_ERROR_TEMPLATE.format(getFilterName()),
            filterName = getFilterName(),
            cause = e
        )
    }
}