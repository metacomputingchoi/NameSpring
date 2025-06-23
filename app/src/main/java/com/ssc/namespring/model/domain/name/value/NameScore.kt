// model/domain/name/value/NameScore.kt
package com.ssc.namespring.model.domain.name.value

data class NameScore(
    val fourTypesLuck: Int,
    val nameElementHarmony: Int,
    val typeElementHarmony: Int,
    val yinYangBalance: Int,
    val sajuComplement: Int,
    val pronunciation: Int,
    val meaning: Int,
    val total: Int
)