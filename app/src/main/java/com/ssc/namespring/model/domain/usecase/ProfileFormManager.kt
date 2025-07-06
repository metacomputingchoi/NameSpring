// model/domain/usecase/ProfileFormManager.kt
package com.ssc.namespring.model.domain.usecase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.domain.entity.NameData
import com.ssc.namespring.model.presentation.components.ProfileFormUiState
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormDateTimeManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormStateManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFactory
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager
import java.util.Calendar

class ProfileFormManager(private val profileId: String? = null) {
    companion object {
        private const val TAG = "ProfileFormManager"
    }

    private val _uiState = MutableLiveData<ProfileFormUiState>()
    val uiState: LiveData<ProfileFormUiState> = _uiState

    private val dateTimeManager = ProfileFormDateTimeManager()
    private val nameDataManager = NameDataManager()
    private val stateManager = ProfileFormStateManager()
    private val profileFactory = ProfileFactory()
    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()

    init {
        nameDataManager.initialize()
        updateUiState()
    }

    fun initialize() {
        if (!profileId.isNullOrEmpty()) {
            profileManager.getProfile(profileId)?.let { profile ->
                loadProfileData(profile)
            }
        } else {
            nameDataManager.initialize()
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
        Log.d(TAG, "setHanjaInfo: position=$position, korean='$korean', hanja='$hanja'")

        // NameDataManager에 데이터 설정
        nameDataManager.setCharData(position, korean, hanja)

        // NameData에서 CharTripleInfo 가져와서 설정
        if (korean.isNotEmpty() && hanja.isNotEmpty()) {
            NameData.getCharInfo(korean, hanja)?.let { info ->
                nameDataManager.setHanjaInfo(position, info)
            }
        }

        // UI 상태 업데이트
        updateUiState()
    }

    fun resetAllFields() {
        dateTimeManager.reset()
        nameDataManager.reset()
        stateManager.reset()
        updateUiState()
    }

    fun createProfile(profileName: String): Profile {
        Log.d(TAG, "Creating profile with name: $profileName")

        // 현재 NameDataManager 상태 확인
        val givenNameInfo = nameDataManager.createGivenNameInfo()
        Log.d(TAG, "GivenNameInfo from NameDataManager:")
        if (givenNameInfo != null) {
            Log.d(TAG, "  Korean: '${givenNameInfo.korean}'")
            Log.d(TAG, "  Hanja: '${givenNameInfo.hanja}'")
            givenNameInfo.charInfos.forEachIndexed { index, charInfo ->
                Log.d(TAG, "  CharInfo[$index]: korean='${charInfo.korean}', hanja='${charInfo.hanja}'")
            }
        } else {
            Log.d(TAG, "  GivenNameInfo is null")
        }

        val profile = profileFactory.createProfile(
            profileId = profileId,
            profileName = profileName,
            birthDate = dateTimeManager.getCalendar(),
            isYajaTime = stateManager.isYajaTime(),
            surname = stateManager.getSurname(),
            givenName = givenNameInfo,
            existingProfile = if (!profileId.isNullOrEmpty()) profileManager.getProfile(profileId) else null
        )

        Log.d(TAG, "Created profile: surname=${profile.surname?.korean}(${profile.surname?.hanja}), " +
                "givenName=${profile.givenName?.korean}(${profile.givenName?.hanja})")

        return profile
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