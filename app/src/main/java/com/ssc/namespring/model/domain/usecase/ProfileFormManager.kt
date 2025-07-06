// model/domain/usecase/ProfileFormManager.kt
package com.ssc.namespring.model.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.presentation.components.ProfileFormUiState
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormDateTimeManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormNameDataManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormStateManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFactory
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager
import java.util.Calendar

class ProfileFormManager(private val profileId: String? = null) {
    private val _uiState = MutableLiveData<ProfileFormUiState>()
    val uiState: LiveData<ProfileFormUiState> = _uiState

    private val dateTimeManager = ProfileFormDateTimeManager()
    private val nameDataManager = ProfileFormNameDataManager()
    private val stateManager = ProfileFormStateManager()
    private val profileFactory = ProfileFactory()
    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()

    init {
        updateUiState()
    }

    fun initialize() {
        if (!profileId.isNullOrEmpty()) {
            profileManager.getProfile(profileId)?.let { profile ->
                loadProfileData(profile)
            }
        } else {
            updateUiState()
        }
    }

    private fun loadProfileData(profile: Profile) {
        stateManager.loadFromProfile(profile)
        dateTimeManager.setDateTime(profile.birthDate)
        nameDataManager.loadFromProfile(profile)
        updateUiState()
    }

    fun updateDate(calendar: Calendar) {
        dateTimeManager.updateDate(calendar)
        updateUiState()
    }

    fun updateTime(calendar: Calendar) {
        dateTimeManager.updateTime(calendar)
        updateUiState()
    }

    fun updateYajaTime(isChecked: Boolean) {
        stateManager.updateYajaTime(isChecked)
        updateUiState()
    }

    fun addNameChar() {
        if (nameDataManager.canAddChar()) {
            nameDataManager.addChar()
            updateUiState()
        }
    }

    fun removeNameChar() {
        if (nameDataManager.canRemoveChar()) {
            nameDataManager.removeChar()
            updateUiState()
        }
    }

    fun setSurname(surname: SurnameInfo?) {
        stateManager.setSurname(surname)
        updateUiState()
    }

    fun setHanjaInfo(position: Int, korean: String, hanja: String) {
        nameDataManager.setCharData(position, korean, hanja)
        updateUiState()
    }

    fun resetAllFields() {
        dateTimeManager.reset()
        nameDataManager.reset()
        stateManager.reset()
        updateUiState()
    }

    fun createProfile(profileName: String): Profile {
        return profileFactory.createProfile(
            profileId = profileId,
            profileName = profileName,
            birthDate = dateTimeManager.getCalendar(),
            isYajaTime = stateManager.isYajaTime(),
            surname = stateManager.getSurname(),
            givenName = nameDataManager.createGivenNameInfo(),
            existingProfile = if (!profileId.isNullOrEmpty()) profileManager.getProfile(profileId) else null
        )
    }

    fun getNameDataManager(): INameDataManager = nameDataManager
    fun getSelectedDate() = dateTimeManager.getCalendar()

    private fun updateUiState() {
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
}