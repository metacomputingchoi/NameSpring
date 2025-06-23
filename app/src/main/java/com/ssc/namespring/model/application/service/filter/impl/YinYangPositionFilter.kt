// model/application/service/filter/impl/YinYangPositionFilter.kt
package com.ssc.namespring.model.application.service.filter.impl

import com.ssc.namespring.model.application.service.filter.NameFilter
import com.ssc.namespring.model.application.service.filter.FilterResult
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.common.constants.FilterConstants

class YinYangPositionFilter : NameFilter() {

    override fun doFilter(name: Name): FilterResult {
        val combinedPm = name.combinedPm ?: return FilterResult(
            passed = false,
            reason = "combined_pm_is_null"
        )

        if (combinedPm[FilterConstants.PM_FIRST_INDEX] == combinedPm[FilterConstants.PM_THIRD_INDEX]) {
            return FilterResult(
                passed = false,
                reason = "pm[0]=${combinedPm[FilterConstants.PM_FIRST_INDEX]} == pm[2]=${combinedPm[FilterConstants.PM_THIRD_INDEX]}"
            )
        }

        return FilterResult(passed = true)
    }

    override fun getFilterName() = "pm_position_check"
}