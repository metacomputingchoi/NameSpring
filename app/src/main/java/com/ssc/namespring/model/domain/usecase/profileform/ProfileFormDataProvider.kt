// model/domain/usecase/profileform/ProfileFormDataProvider.kt
package com.ssc.namespring.model.domain.usecase.profileform

import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.usecase.NameDataManager
import com.ssc.namespring.model.presentation.components.ProfileFormUiState

internal class ProfileFormDataProvider(
    private val nameDataManager: NameDataManager,
    private val stateManager: ProfileFormStateManager
) {

    fun getGivenNameInfo(): GivenNameInfo? = nameDataManager.createGivenNameInfo()

    fun getGivenNameData(uiState: ProfileFormUiState): GivenNameData {
        val korean = uiState.nameCharDataList.joinToString("") { it.korean }
        val hanja = uiState.nameCharDataList.joinToString("") { it.hanja }
        val charInfos = uiState.nameCharDataList.map { charData ->
            mapOf(
                "korean" to charData.korean,
                "hanja" to charData.hanja
            )
        }

        return GivenNameData(
            korean = korean,
            hanja = hanja,
            charInfos = charInfos,
            charCount = uiState.nameCharCount
        )
    }
}