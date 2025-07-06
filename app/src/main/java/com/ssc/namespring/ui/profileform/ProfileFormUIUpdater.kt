// ui/profileform/ProfileFormUIUpdater.kt
package com.ssc.namespring.ui.profileform

import android.annotation.SuppressLint
import android.view.View
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.entity.ProfileFormMode
import com.ssc.namespring.model.presentation.components.ProfileFormUiState

class ProfileFormUIUpdater(
    private val uiComponents: ProfileFormUIComponents,
    private val nameInputHandler: ProfileFormNameInputHandler,
    private val config: ProfileFormConfig
) {
    fun updateUI(state: ProfileFormUiState) {
        updateProfileName(state)
        updateBirthInfo(state)
        updateSurnameInfo(state)
        updateNameCharCount(state)
        nameInputHandler.refreshNameInputViews(
            uiComponents.nameInputContainer,
            state
        )
    }

    private fun updateProfileName(state: ProfileFormUiState) {
        // 작명/평가 모드에서는 사용자가 입력한 값이 없으면 기본값 유지
        if (config.mode in listOf(ProfileFormMode.NAMING, ProfileFormMode.EVALUATION)) {
            if (uiComponents.etProfileName.text.toString().isEmpty() && state.profileName.isEmpty()) {
                uiComponents.etProfileName.setText(config.getDefaultName())
            }
        } else {
            if (uiComponents.etProfileName.text.toString() != state.profileName &&
                state.profileName.isNotEmpty()) {
                uiComponents.etProfileName.setText(state.profileName)
            }
        }
    }

    private fun updateBirthInfo(state: ProfileFormUiState) {
        uiComponents.tvBirthDate.text = state.birthDateText
        uiComponents.tvBirthTime.text = state.birthTimeText
        uiComponents.cbYajaTime.isChecked = state.isYajaTime
    }

    @SuppressLint("SetTextI18n")
    private fun updateSurnameInfo(state: ProfileFormUiState) {
        if (state.selectedSurname != null) {
            uiComponents.tvSelectedSurname.text =
                "${state.selectedSurname.korean}(${state.selectedSurname.hanja})"
            uiComponents.tvSelectedSurname.visibility = View.VISIBLE
        } else {
            uiComponents.tvSelectedSurname.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateNameCharCount(state: ProfileFormUiState) {
        uiComponents.tvCharCount.text = "${state.nameCharCount}글자"
        uiComponents.btnAddChar.isEnabled = state.nameCharCount < 4
        uiComponents.btnRemoveChar.isEnabled = state.nameCharCount > 1
    }
}