// model/data/FilterContext.kt
package com.ssc.namespring.model.data

data class FilterContext(
    val surHangul: String,
    val surLength: Int,
    val nameLength: Int,
    val dictElementsCount: Map<String, Int> = emptyMap()
)
