// model/data/GeneratedName.kt
package com.ssc.namespring.model.data

data class GeneratedName(
    val surnameHangul: String,
    val surnameHanja: String,
    val combinedHanja: String,
    val combinedPronounciation: String,
    val fourTypes: FourTypes,
    val nameStrokes: List<Int>,
    val hanjaDetails: List<HanjaInfo>
)
