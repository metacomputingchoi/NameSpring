// model/domain/coordinator/ProfileFormUiCoordinator.kt
package com.ssc.namespring.model.domain.coordinator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.presentation.components.ProfileFormUiState
import com.ssc.namespring.model.domain.usecase.NameDataManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormDateTimeManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormStateManager

/**
 * ProfileForm의 UI 상태를 관리하는 코디네이터
 */
class ProfileFormUiCoordinator(
    private val dateTimeManager: ProfileFormDateTimeManager,
    private val nameDataManager: NameDataManager,
    private val stateManager: ProfileFormStateManager
) {

    private val _uiState = MutableLiveData<ProfileFormUiState>()
    val uiState: LiveData<ProfileFormUiState> = _uiState

    private val _profileLoaded = MutableLiveData<Boolean>()
    val profileLoaded: LiveData<Boolean> = _profileLoaded

    init {
        updateState()
    }

    fun updateState() {
        _uiState.value = ProfileFormUiState(
            profileName = stateManager.getProfileName(),
            birthDateText = dateTimeManager.getFormattedDate(),
            birthTimeText = dateTimeManager.getFormattedTime(),
            isYajaTime = stateManager.isYajaTime(),
            selectedSurname = stateManager.getSurname(),
            nameCharCount = nameDataManager.getCharCount(),
            nameCharDataList = nameDataManager.getCharDataList()
        )
    }

    fun setProfileLoaded(loaded: Boolean) {
        _profileLoaded.value = loaded
    }

    fun resetProfileLoadedFlag() {
        _profileLoaded.value = false
    }
}
