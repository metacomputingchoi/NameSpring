// model/business/ProfileFormUiState.kt
package com.ssc.namespring.model.business

import com.ssc.namespring.model.data.SurnameInfo

data class ProfileFormUiState(
    val profileName: String = "",
    val birthDateText: String = "",
    val birthTimeText: String = "",
    val isYajaTime: Boolean = true,
    val selectedSurname: SurnameInfo? = null,
    val nameCharCount: Int = 1,
    val nameCharDataList: List<NameCharData> = listOf(NameCharData())
)

data class NameCharData(
    var korean: String = "",
    var hanja: String = ""
)