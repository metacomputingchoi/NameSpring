// model/filter/NameFilterStrategy.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.FilteringStep

interface NameFilterStrategy {
    fun filter(names: List<GeneratedName>, context: FilterContext): List<GeneratedName>
    fun filterBatch(names: Sequence<GeneratedName>, context: FilterContext): Sequence<GeneratedName>

    // 필터링하지 않고 평가만 수행
    fun evaluate(name: GeneratedName, context: FilterContext): FilteringStep
}