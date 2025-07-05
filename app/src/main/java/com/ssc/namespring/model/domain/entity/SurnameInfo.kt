// model/domain/entity/SurnameInfo.kt
package com.ssc.namespring.model.domain.entity

import java.io.Serializable

data class SurnameInfo(
    val korean: String,
    val hanja: String,
    val meaning: String? = null,
    val strokes: Int = 0,
    val ohaeng: String? = null,
    val eumyang: Int = 0
) : Serializable