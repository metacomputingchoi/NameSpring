// model/domain/usecase/profileform/GivenNameData.kt
package com.ssc.namespring.model.domain.usecase.profileform

data class GivenNameData(
    val korean: String,
    val hanja: String,
    val charInfos: List<Map<String, String>>,
    val charCount: Int
)