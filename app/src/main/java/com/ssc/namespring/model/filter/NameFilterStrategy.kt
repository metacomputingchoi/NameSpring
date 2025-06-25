// model/filter/NameFilterStrategy.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName

interface NameFilterStrategy {
    fun filter(names: List<GeneratedName>, context: FilterContext): List<GeneratedName>
    fun filterBatch(names: Sequence<GeneratedName>, context: FilterContext): Sequence<GeneratedName>
}
