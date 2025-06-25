// model/data/HanjaInfo.kt
package com.ssc.namespring.model.data

data class HanjaInfo(
    val hanja: String,
    val inmyongMeaning: String,
    val inmyongSound: String,
    val baleumEumyang: String,
    val hoeksuEumyang: String,
    val baleumOhaeng: String,
    val jawonOhaeng: String,
    val wonHoeksu: Int,
    val okpyeonHoeksu: Int
)
