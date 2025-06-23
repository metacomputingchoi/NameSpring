// model/domain/hanja/entity/Hanja.kt
package com.ssc.namespring.model.domain.hanja.entity

data class Hanja(
    val hanja: String,
    val inmyeongYongEum: String?,
    val inmyeongYongDdeut: String?,
    val wonHoeksu: Int,
    val jawonOheng: String?,
    val baleumOheng: String?,
    val cautionRed: String?,
    val cautionBlue: String?
)