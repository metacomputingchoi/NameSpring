// model/filter/core/NameFilter.kt
package com.ssc.namespring.model.filter.core

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.FilteringStep

interface NameFilter {
    fun getName(): String
    fun filter(names: List<GeneratedName>, context: FilterContext): List<GeneratedName>
    fun filterBatch(names: Sequence<GeneratedName>, context: FilterContext): Sequence<GeneratedName>
    fun evaluate(name: GeneratedName, context: FilterContext): FilteringStep
}