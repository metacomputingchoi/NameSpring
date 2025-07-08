// model/domain/usecase/profilelist/IProfileListHandler.kt
package com.ssc.namespring.model.domain.usecase.profilelist

import android.content.Context
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.presentation.components.ProfileListUiState

interface IProfileListHandler {
    fun handleAction(context: Context? = null, vararg params: Any): Any?
}

interface IProfileListUiStateManager {
    fun updateUiState(update: (ProfileListUiState) -> ProfileListUiState)
    fun getCurrentState(): ProfileListUiState?
}
