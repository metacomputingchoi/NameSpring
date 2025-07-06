// model/domain/usecase/ProfileFormManager.kt
package com.ssc.namespring.model.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.presentation.components.ProfileFormUiState
import com.ssc.namespring.model.common.utils.DateTimeManager
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SurnameInfo
import java.util.Calendar

class ProfileFormManager(private val profileId: String? = null) {
    private val _uiState = MutableLiveData<ProfileFormUiState>()
    val uiState: LiveData<ProfileFormUiState> = _uiState

    private val dateTimeManager = DateTimeManager()
    private val nameDataManager = NameDataManager()
    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()

    init {
        _uiState.value = ProfileFormUiState()
    }

    fun initialize() {
        nameDataManager.initialize()

        if (!profileId.isNullOrEmpty()) {
            profileManager.getProfile(profileId)?.let { profile ->
                loadProfileData(profile)
            }
        } else {
            updateUiState()
        }
    }

    private fun loadProfileData(profile: Profile) {
        dateTimeManager.setDateTime(profile.birthDate)
        nameDataManager.loadFromProfile(profile)

        _uiState.value = ProfileFormUiState(
            profileName = profile.profileName,
            birthDateText = dateTimeManager.getFormattedDate(),
            birthTimeText = dateTimeManager.getFormattedTime(),
            isYajaTime = profile.isYajaTime,
            selectedSurname = profile.surname,
            nameCharCount = nameDataManager.getCharCount(),
            nameCharDataList = nameDataManager.getCharDataList()
        )
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
        _uiState.value = _uiState.value?.copy(isYajaTime = isChecked)
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
        _uiState.value = _uiState.value?.copy(selectedSurname = surname)
    }

    fun setHanjaInfo(position: Int, korean: String, hanja: String) {
        nameDataManager.setCharData(position, korean, hanja)
        updateUiState()
    }

    fun resetAllFields() {
        dateTimeManager.reset()
        nameDataManager.reset()
        _uiState.value = ProfileFormUiState()
    }

    fun createProfile(profileName: String): Profile {
        val givenName = nameDataManager.createGivenNameInfo()

        return if (!profileId.isNullOrEmpty()) {
            // 기존 프로필 업데이트
            Profile(
                id = profileId,
                profileName = profileName,
                birthDate = dateTimeManager.getCalendar(),
                isYajaTime = _uiState.value?.isYajaTime == true,
                surname = _uiState.value?.selectedSurname,
                givenName = givenName,
                createdAt = profileManager.getProfile(profileId)?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        } else {
            // 새 프로필 생성
            Profile(
                profileName = profileName,
                birthDate = dateTimeManager.getCalendar(),
                isYajaTime = _uiState.value?.isYajaTime == true,
                surname = _uiState.value?.selectedSurname,
                givenName = givenName
            )
        }
    }

    fun getNameDataManager() = nameDataManager
    fun getSelectedDate() = dateTimeManager.getCalendar()

    private fun updateUiState() {
        val currentState = _uiState.value ?: ProfileFormUiState()
        _uiState.value = currentState.copy(
            birthDateText = dateTimeManager.getFormattedDate(),
            birthTimeText = dateTimeManager.getFormattedTime(),
            nameCharCount = nameDataManager.getCharCount(),
            nameCharDataList = nameDataManager.getCharDataList()
        )
    }
}