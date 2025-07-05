// model/domain/entity/GivenNameInfo.kt
package com.ssc.namespring.model.domain.entity

import java.io.Serializable

data class GivenNameInfo(
    val korean: String,
    val hanja: String,
    val charInfos: List<CharInfo> = emptyList()
) : Serializable