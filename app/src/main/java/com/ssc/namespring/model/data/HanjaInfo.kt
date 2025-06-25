// model/data/HanjaInfo.kt
package com.ssc.namespring.model.data

data class HanjaInfo(
    val hanja: String,
    val inmyongMeaning: String,
    val inmyongSound: String,
    val pronunciationYinYang: String,
    val strokeYinYang: String,
    val pronunciationElement: String,
    val sourceElement: String,
    val originalStroke: Int,
    val dictionaryStroke: Int
)
