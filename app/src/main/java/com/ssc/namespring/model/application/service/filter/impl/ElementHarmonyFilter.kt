// model/application/service/filter/impl/ElementHarmonyFilter.kt
package com.ssc.namespring.model.application.service.filter.impl

import com.ssc.namespring.model.application.service.filter.NameFilter
import com.ssc.namespring.model.application.service.filter.FilterResult
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.common.util.ElementUtils

class ElementHarmonyFilter : NameFilter() {

    override fun doFilter(name: Name): FilterResult {
        val combinedElement = name.combinedElement ?: return FilterResult(
            passed = false,
            reason = "combined_element_is_null"
        )

        val (isHarmonious, harmonyDetails) = ElementUtils.isHarmoniousElementCombination(combinedElement)

        if (!isHarmonious) {
            return FilterResult(
                passed = false,
                details = mapOf("harmony_details" to harmonyDetails)
            )
        }

        return FilterResult(
            passed = true,
            details = mapOf("harmony_details" to harmonyDetails)
        )
    }

    override fun getFilterName() = "element_harmony_check"
}