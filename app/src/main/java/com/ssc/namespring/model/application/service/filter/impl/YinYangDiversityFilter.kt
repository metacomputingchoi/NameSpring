// model/application/service/filter/impl/YinYangDiversityFilter.kt
package com.ssc.namespring.model.application.service.filter.impl

import com.ssc.namespring.model.application.service.filter.NameFilter
import com.ssc.namespring.model.application.service.filter.FilterResult
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.common.constants.Constants

class YinYangDiversityFilter : NameFilter() {

    override fun doFilter(name: Name): FilterResult {
        val combinedPm = name.combinedPm ?: return FilterResult(
            passed = false,
            reason = "combined_pm_is_null"
        )

        val pmSet = combinedPm.toSet()

        if (pmSet.size <= Constants.MIN_PM_DIVERSITY) {
            return FilterResult(
                passed = false,
                reason = "pm_set=$pmSet"
            )
        }

        return FilterResult(
            passed = true,
            details = mapOf("pm_set" to pmSet.toList())
        )
    }

    override fun getFilterName() = "pm_diversity_check"
}