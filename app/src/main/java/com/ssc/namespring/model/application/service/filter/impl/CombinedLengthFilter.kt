// model/application/service/filter/impl/CombinedLengthFilter.kt
package com.ssc.namespring.model.application.service.filter.impl

import com.ssc.namespring.model.application.service.filter.NameFilter
import com.ssc.namespring.model.application.service.filter.FilterResult
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.common.constants.Constants

class CombinedLengthFilter : NameFilter() {

    override fun doFilter(name: Name): FilterResult {
        val combinedElement = name.combinedElement
        val combinedPm = name.combinedPm

        if (combinedElement == null || combinedPm == null) {
            return FilterResult(
                passed = false,
                reason = "combined_element_or_pm_is_null"
            )
        }

        if (combinedElement.length != Constants.COMBINED_LENGTH ||
            combinedPm.length != Constants.COMBINED_LENGTH) {
            return FilterResult(
                passed = false,
                reason = "combined_element_length=${combinedElement.length}, combined_pm_length=${combinedPm.length}"
            )
        }

        return FilterResult(passed = true)
    }

    override fun getFilterName() = "combined_length_check"
}