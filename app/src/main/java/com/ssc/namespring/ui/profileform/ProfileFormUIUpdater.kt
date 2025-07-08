// ui/profileform/ProfileFormUIUpdater.kt
package com.ssc.namespring.ui.profileform

import android.annotation.SuppressLint
import android.view.View
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.entity.ProfileFormMode
import com.ssc.namespring.model.domain.usecase.ProfileFormManager
import com.ssc.namespring.model.presentation.components.ProfileFormUiState

class ProfileFormUIUpdater(
    private val uiComponents: ProfileFormUIComponents,
    private val nameInputHandler: ProfileFormNameInputHandler,
    private val config: ProfileFormConfig,
    private val formManager: ProfileFormManager
) {
    private var lastUpdateTime = 0L
    private val UPDATE_DEBOUNCE_MS = 100L // 100ms 디바운싱

    fun updateUI(state: ProfileFormUiState) {
        // 디바운싱: 너무 빈번한 업데이트 방지
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < UPDATE_DEBOUNCE_MS) {
            return
        }
        lastUpdateTime = currentTime

        updateProfileName(state)
        updateBirthInfo(state)
        updateSurnameInfo(state)
        updateNameCharCount(state)

        // 프로필 로드 여부 체크
        val forceRecreate = formManager.profileLoaded.value == true

        nameInputHandler.refreshNameInputViews(
            uiComponents.nameInputContainer,
            state,
            forceRecreate
        )

        // 프로필 로드 플래그 리셋 - 한 번만 forceRecreate가 true가 되도록
        if (forceRecreate) {
            formManager.resetProfileLoadedFlag()
        }
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