// model/application/service/filter/impl/NaturalnessFilter.kt
package com.ssc.namespring.model.application.service.filter.impl

import com.ssc.namespring.model.application.service.filter.NameFilter
import com.ssc.namespring.model.application.service.filter.FilterResult
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.infrastructure.repository.NameRepository

class NaturalnessFilter(private val nameRepository: NameRepository) : NameFilter() {

    override fun doFilter(name: Name): FilterResult {
        val combinedPronounciation = name.combinedPronounciation ?: return FilterResult(
            passed = false,
            reason = "combined_pronunciation_is_null"
        )

        if (nameRepository.existsHangulName(combinedPronounciation)) {
            return FilterResult(
                passed = true,
                details = mapOf(
                    "name" to combinedPronounciation,
                    "name_data" to nameRepository.getHangulNameData(combinedPronounciation)
                )
            )
        } else {
            return FilterResult(
                passed = false,
                details = mapOf("name" to combinedPronounciation)
            )
        }
    }

    override fun getFilterName() = "hangul_naturalness_check"
}