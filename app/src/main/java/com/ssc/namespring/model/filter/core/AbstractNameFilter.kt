// model/filter/core/AbstractNameFilter.kt
package com.ssc.namespring.model.filter.core

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.FilteringStep
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.filter.constants.FilterConstants

abstract class AbstractNameFilter : NameFilter {

    override fun filter(names: List<GeneratedName>, context: FilterContext): List<GeneratedName> {
        return names.filter { name -> isValidName(name, context) }
    }

    override fun filterBatch(names: Sequence<GeneratedName>, context: FilterContext): Sequence<GeneratedName> {
        return names.filter { name -> isValidName(name, context) }
    }

    override fun evaluate(name: GeneratedName, context: FilterContext): FilteringStep {
        return try {
            val validationResult = getValidationDetails(name, context)
            createFilteringStep(validationResult)
        } catch (e: Exception) {
            createErrorFilteringStep(e)
        }
    }

    protected abstract fun getValidationDetails(name: GeneratedName, context: FilterContext): ValidationResult

    private fun isValidName(name: GeneratedName, context: FilterContext): Boolean {
        return try {
            getValidationDetails(name, context).isValid
        } catch (e: Exception) {
            handleFilterError(e)
        }
    }

    protected fun createFilteringStep(validationResult: ValidationResult): FilteringStep {
        return FilteringStep(
            filterName = getName(),
            passed = validationResult.isValid,
            reason = validationResult.reason,
            details = validationResult.details
        )
    }

    private fun createErrorFilteringStep(e: Exception): FilteringStep {
        return FilteringStep(
            filterName = getName(),
            passed = false,
            reason = FilterConstants.EVALUATION_ERROR_TEMPLATE.format(e.message),
            details = emptyMap()
        )
    }

    private fun handleFilterError(e: Exception): Nothing {
        throw NamingException.FilteringException(
            FilterConstants.FILTER_ERROR_TEMPLATE.format(getName()),
            filterName = getName(),
            cause = e
        )
    }
}