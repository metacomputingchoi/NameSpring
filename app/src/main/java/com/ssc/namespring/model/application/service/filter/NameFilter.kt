// model/application/service/filter/NameFilter.kt
package com.ssc.namespring.model.application.service.filter

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.entity.FilteringStep

abstract class NameFilter {
    private var nextFilter: NameFilter? = null

    fun setNext(filter: NameFilter): NameFilter {
        nextFilter = filter
        return filter
    }

    fun filter(name: Name): FilterResult {
        val result = doFilter(name)
        name.filteringProcess.add(
            FilteringStep(
                step = getFilterName(),
                passed = result.passed,
                reason = result.reason,
                details = result.details
            )
        )

        if (result.passed && nextFilter != null) {
            return nextFilter!!.filter(name)
        }

        return result
    }

    protected abstract fun doFilter(name: Name): FilterResult
    protected abstract fun getFilterName(): String
}

data class FilterResult(
    val passed: Boolean,
    val reason: String? = null,
    val details: Map<String, Any>? = null
)