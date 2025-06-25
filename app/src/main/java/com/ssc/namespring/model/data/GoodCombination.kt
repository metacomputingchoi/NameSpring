// model/data/GoodCombination.kt
package com.ssc.namespring.model.data

data class GoodCombination(
    val nameStrokes: List<Int>,
    val fourTypes: FourTypes,
    val namePN: List<Int>,
    val nameElements: List<Int>,
    val typeElements: List<Int>
)
