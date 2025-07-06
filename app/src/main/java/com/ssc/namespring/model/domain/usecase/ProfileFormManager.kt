// model/domain/usecase/ProfileFormManager.kt
package com.ssc.namespring.model.domain.usecase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.presentation.components.ProfileFormUiState
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormDateTimeManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormStateManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFactory
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import com.ssc.namespring.model.domain.service.factory.NameDataServiceFactory
import com.ssc.namespring.model.domain.service.profile.ProfileEvaluationService
import com.ssc.namingengine.NamingEngine
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
    private val nameDataService: INameDataService = NameDataServiceFactory.getInstance()

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
        // 재평가 없이 기존 데이터만 로드
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

        // INameDataService를 통해 CharTripleInfo 가져와서 설정
        if (korean.isNotEmpty() && hanja.isNotEmpty()) {
            nameDataService.getCharInfo(korean, hanja)?.let { info ->
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

    fun loadFromParentProfile(parentProfile: Profile) {
        // 날짜/시간 정보 로드
        dateTimeManager.setDateTime(parentProfile.birthDate)
        stateManager.updateYajaTime(parentProfile.isYajaTime)

        // 성씨 정보 로드
        stateManager.setSurname(parentProfile.surname)

        // 이름 정보 로드 (길이까지 동일하게)
        parentProfile.givenName?.let { givenName ->
            // 기존 이름 데이터 리셋
            nameDataManager.reset()

            // 부모 프로필의 이름 길이만큼 글자 추가
            val targetCount = givenName.charInfos.size
            repeat(targetCount - 1) { // 기본 1글자이므로 targetCount-1만큼 추가
                nameDataManager.addChar()
            }

            // 각 글자의 한글과 한자 정보 설정
            givenName.charInfos.forEachIndexed { index, charInfo ->
                if (charInfo.korean.isNotEmpty() && charInfo.hanja.isNotEmpty()) {
                    setHanjaInfo(index, charInfo.korean, charInfo.hanja)
                }
            }
        }

        updateUiState()
    }

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