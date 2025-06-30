// filter/core/FilterChain.kt
package com.ssc.namingengine.filter.core

import com.ssc.namingengine.data.FilterContext
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namingengine.data.analysis.FilteringStep

class FilterChain {
    private val filters = mutableListOf<NameFilter>()

    fun addFilter(filter: NameFilter): FilterChain {
        filters.add(filter)
        return this
    }

    fun filter(names: List<GeneratedName>, context: FilterContext): List<GeneratedName> {
        return filters.fold(names) { acc, filter ->
            filter.filter(acc, context)
        }
    }

    fun filterBatch(names: Sequence<GeneratedName>, context: FilterContext): Sequence<GeneratedName> {
        return filters.fold(names) { acc, filter ->
            filter.filterBatch(acc, context)
        }
    }

    fun evaluateAll(name: GeneratedName, context: FilterContext): List<FilteringStep> {
        return filters.map { filter ->
            filter.evaluate(name, context)
        }
    }
}