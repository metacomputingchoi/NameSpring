// model/domain/usecase/ProfileFormManager.kt
package com.ssc.namespring.model.domain.usecase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.domain.entity.GivenNameInfo
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
import java.time.LocalDateTime
import java.time.ZoneId
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

    // 작명 모드 검증
    fun isValidForNaming(): Boolean {
        val surname = getSurname()
        if (surname == null || surname.korean.isEmpty() || surname.hanja.isEmpty()) {
            Log.w(TAG, "작명 모드: 성씨 정보가 불완전합니다")
            return false
        }

        val nameCharCount = nameDataManager.getCharCount()
        if (nameCharCount !in 1..4) {
            Log.w(TAG, "작명 모드: 이름은 1~4글자여야 합니다")
            return false
        }

        return true
    }

    // 평가 모드 검증
    fun isValidForEvaluation(): Boolean {
        val surname = getSurname()
        if (surname == null || surname.korean.isEmpty() || surname.hanja.isEmpty()) {
            Log.w(TAG, "평가 모드: 성씨 정보가 불완전합니다")
            return false
        }

        val givenNameInfo = getGivenNameInfo()
        if (givenNameInfo == null || givenNameInfo.charInfos.isEmpty()) {
            Log.w(TAG, "평가 모드: 이름 정보가 없습니다")
            return false
        }

        val nameCharCount = givenNameInfo.charInfos.size
        if (nameCharCount !in 1..4) {
            Log.w(TAG, "평가 모드: 이름은 1~4글자여야 합니다")
            return false
        }

        // 모든 이름 글자가 한글+한자 모두 입력되었는지 확인
        val allFilled = givenNameInfo.charInfos.all { charInfo ->
            charInfo.korean.isNotEmpty() && charInfo.hanja.isNotEmpty()
        }

        if (!allFilled) {
            Log.w(TAG, "평가 모드: 모든 이름의 한글과 한자가 입력되어야 합니다")
            return false
        }

        return true
    }

    // 작명용 입력 생성 (입력된 정보는 유지, 빈 곳만 언더바 처리)
    fun createNamingInput(): NamingEngineInput? {
        val surname = getSurname() ?: return null

        // 성씨 부분
        var userInput = "[${surname.korean}/${surname.hanja}]"

        // 이름 부분 - 각 글자별로 입력된 정보 확인
        val charDataList = nameDataManager.getCharDataList()

        Log.d(TAG, "=== createNamingInput Debug ===")
        Log.d(TAG, "Surname: ${surname.korean}/${surname.hanja}")
        Log.d(TAG, "CharDataList size: ${charDataList.size}")

        charDataList.forEachIndexed { index, charData ->
            Log.d(TAG, "CharData[$index]: korean='${charData.korean}', hanja='${charData.hanja}'")

            val korean = if (charData.korean.isNotEmpty()) charData.korean else "_"
            val hanja = if (charData.hanja.isNotEmpty()) charData.hanja else "_"
            userInput += "[$korean/$hanja]"
        }

        Log.d(TAG, "Final userInput: $userInput")

        val birthDateTime = dateTimeManager.getCalendar().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        return NamingEngineInput(
            userInput = userInput,
            birthDateTime = birthDateTime,
            useYajasi = isYajaTime()
        )
    }

    // 평가용 입력 생성 (전체 이름 포함)
    fun createEvaluationInput(): NamingEngineInput? {
        return createNamingEngineInput() // 기존 메서드 사용 (전체 이름 포함)
    }

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

    fun getSurname(): SurnameInfo? = stateManager.getSurname()
    fun isYajaTime(): Boolean = stateManager.isYajaTime()

    // NamingEngine 입력 형식으로 변환하는 메서드 추가
    fun createNamingEngineInput(): NamingEngineInput? {
        val surname = getSurname()  // 내부 메서드 사용
        val givenNameInfo = nameDataManager.createGivenNameInfo()

        if (surname == null) {
            Log.w(TAG, "성씨 정보가 없습니다")
            return null
        }

        // NamingEngine 입력 형식 생성
        val userInput = buildUserInput(surname, givenNameInfo)
        val birthDateTime = dateTimeManager.getCalendar().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val useYajasi = isYajaTime()  // 내부 메서드 사용

        return NamingEngineInput(
            userInput = userInput,
            birthDateTime = birthDateTime,
            useYajasi = useYajasi
        )
    }

    private fun buildUserInput(surname: SurnameInfo, givenNameInfo: GivenNameInfo?): String {
        // 성씨 부분
        val surnameInput = "[${surname.korean}/${surname.hanja}]"

        // 이름 부분
        val givenNameInput = if (givenNameInfo != null) {
            givenNameInfo.charInfos.joinToString("") { charInfo ->
                if (charInfo.korean.isNotEmpty() && charInfo.hanja.isNotEmpty()) {
                    "[${charInfo.korean}/${charInfo.hanja}]"
                } else {
                    ""
                }
            }
        } else {
            ""
        }

        return surnameInput + givenNameInput
    }

    // 추가: 생년월일시 정보 가져오기
    fun getBirthDateTime(): Calendar = dateTimeManager.getCalendar()

    // 추가: 이름 정보 가져오기
    fun getGivenNameInfo(): GivenNameInfo? = nameDataManager.createGivenNameInfo()

    // NamingEngine 입력 데이터를 담는 data class
    data class NamingEngineInput(
        val userInput: String,
        val birthDateTime: LocalDateTime,
        val useYajasi: Boolean
    )

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