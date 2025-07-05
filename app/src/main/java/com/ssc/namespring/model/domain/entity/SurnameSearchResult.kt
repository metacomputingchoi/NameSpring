// model/domain/entity/SurnameSearchResult.kt
package com.ssc.namespring.model.domain.entity

data class SurnameSearchResult(
    val korean: String,
    val hanja: String,
    val meaning: String?,
    val isCompound: Boolean
)