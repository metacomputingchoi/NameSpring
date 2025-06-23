// model/application/service/filter/impl/ElementExtractionFilter.kt
package com.ssc.namespring.model.application.service.filter.impl

import com.ssc.namespring.model.application.service.filter.NameFilter
import com.ssc.namespring.model.application.service.filter.FilterResult
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.common.util.HangulUtils

class ElementExtractionFilter : NameFilter() {

    override fun doFilter(name: Name): FilterResult {
        val combinedPronounciation = name.combinedPronounciation ?: return FilterResult(
            passed = false,
            reason = "combined_pronunciation_is_null"
        )

        val elem1 = HangulUtils.getHangulElement(combinedPronounciation[0])
        val elem2 = HangulUtils.getHangulElement(combinedPronounciation[1])

        if (elem1 == null || elem2 == null) {
            return FilterResult(
                passed = false,
                reason = "elem1=$elem1, elem2=$elem2"
            )
        }

        val combinedElement = name.surHangulElement + elem1 + elem2
        val combinedPm = "${name.surHangulPm}${HangulUtils.getHangulPn(combinedPronounciation[0])}${HangulUtils.getHangulPn(combinedPronounciation[1])}"

        name.combinedElement = combinedElement
        name.combinedPm = combinedPm

        return FilterResult(
            passed = true,
            details = mapOf("elem1" to elem1, "elem2" to elem2)
        )
    }

    override fun getFilterName() = "element_extraction"
}