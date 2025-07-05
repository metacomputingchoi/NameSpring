// model/repository/SurnameSearchResult.kt
package com.ssc.namespring.model.repository

data class SurnameSearchResult(
    val korean: String,
    val hanja: String,
    val meaning: String?,
    val isCompound: Boolean
)