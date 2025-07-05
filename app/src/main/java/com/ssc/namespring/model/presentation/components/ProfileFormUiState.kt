// model/presentation/components/ProfileFormUiState.kt
package com.ssc.namespring.model.presentation.components

import com.ssc.namespring.model.domain.entity.SurnameInfo

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