// model/domain/entity/HanjaSearchResult.kt
package com.ssc.namespring.model.domain.entity

data class HanjaSearchResult(
    val korean: String,
    val hanja: String,
    val meaning: String?,
    val ohaeng: String,
    val strokes: Int,
    val soundCount: Int,
    val tripleKey: String
)