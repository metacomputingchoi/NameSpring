// model/domain/usecase/ProfileFormManager.kt
package com.ssc.namespring.model.domain.usecase

import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.R
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

    private var isSettingHanjaInfo = false
    private val _uiState = MutableLiveData<ProfileFormUiState>()
    val uiState: LiveData<ProfileFormUiState> = _uiState
    private val _profileLoaded = MutableLiveData<Boolean>()
    val profileLoaded: LiveData<Boolean> = _profileLoaded
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

    fun resetProfileLoadedFlag() {
        _profileLoaded.value = false
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
        if (isSettingHanjaInfo) return  // 이미 처리 중이면 무시

        // 값이 같으면 업데이트 하지 않음
        val currentData = nameDataManager.getCharData(position)
        if (currentData?.korean == korean && currentData.hanja == hanja) {
            return
        }

        isSettingHanjaInfo = true
        try {
            nameDataManager.setCharData(position, korean, hanja)

            if (korean.isNotEmpty() && hanja.isNotEmpty()) {
                nameDataService.getCharInfo(korean, hanja)?.let { info ->
                    nameDataManager.setHanjaInfo(position, info)
                }
            }

            // UI 업데이트는 한 번만
            updateUiState()
        } finally {
            isSettingHanjaInfo = false
        }
    }

    fun resetAllFields() {
        dateTimeManager.reset()
        nameDataManager.reset()
        stateManager.reset()
        updateUiState()
    }

    fun syncWithUiValues(containerView: LinearLayout) {
        // 현재 UI의 값을 직접 읽어서 NameDataManager 업데이트
        for (i in 0 until containerView.childCount) {
            val itemView = containerView.getChildAt(i)
            val etKorean = itemView?.findViewById<EditText>(R.id.etKorean)
            val etHanja = itemView?.findViewById<EditText>(R.id.etHanja)

            if (etKorean != null) {
                val korean = etKorean.text.toString()
                val hanja = etHanja?.text?.toString() ?: ""

                // 직접 NameDataManager 업데이트
                nameDataManager.setCharData(i, korean, hanja)
            }
        }

        // 상태 업데이트
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

    fun createNamingInput(): NamingEngineInput? {
        val surname = getSurname() ?: return null
        var userInput = "[${surname.korean}/${surname.hanja}]"

        // UI의 현재 상태를 직접 가져오기 (NameDataManager 대신)
        val currentUiState = _uiState.value ?: return null

        Log.d(TAG, "=== createNamingInput Debug ===")
        Log.d(TAG, "Surname: ${surname.korean}/${surname.hanja}")
        Log.d(TAG, "UI State CharDataList size: ${currentUiState.nameCharDataList.size}")

        currentUiState.nameCharDataList.forEachIndexed { index, charData ->
            Log.d(TAG, "UI CharData[$index]: korean='${charData.korean}', hanja='${charData.hanja}'")

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
        // UI 업데이트 일시 중지
        val updates = mutableListOf<() -> Unit>()

        // 날짜/시간 정보 로드
        dateTimeManager.setDateTime(parentProfile.birthDate)
        stateManager.updateYajaTime(parentProfile.isYajaTime)

        // 성씨 정보 로드
        stateManager.setSurname(parentProfile.surname)

        // 이름 정보 로드
        parentProfile.givenName?.let { givenName ->
            nameDataManager.reset()

            // 글자 수 맞추기
            val targetCount = givenName.charInfos.size
            repeat(targetCount - 1) {
                nameDataManager.addChar()
            }

            // 모든 데이터를 먼저 설정
            givenName.charInfos.forEachIndexed { index, charInfo ->
                if (charInfo.korean.isNotEmpty() && charInfo.hanja.isNotEmpty()) {
                    updates.add {
                        nameDataManager.setCharData(index, charInfo.korean, charInfo.hanja)
                        nameDataService.getCharInfo(charInfo.korean, charInfo.hanja)?.let { info ->
                            nameDataManager.setHanjaInfo(index, info)
                        }
                    }
                }
            }
        }

        // 모든 업데이트 실행
        updates.forEach { it() }

        // 한 번만 UI 업데이트
        updateUiState()

        // UI 재생성 플래그 설정
        _profileLoaded.value = true
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

    // 추가: 이름 정보 가져오기
    fun getGivenNameInfo(): GivenNameInfo? = nameDataManager.createGivenNameInfo()

    fun getGivenNameData(): GivenNameData {
        val state = uiState.value ?: ProfileFormUiState()
        val korean = state.nameCharDataList.joinToString("") { it.korean }
        val hanja = state.nameCharDataList.joinToString("") { it.hanja }
        val charInfos = state.nameCharDataList.map { charData ->
            mapOf(
                "korean" to charData.korean,
                "hanja" to charData.hanja
            )
        }

        return GivenNameData(
            korean = korean,
            hanja = hanja,
            charInfos = charInfos,
            charCount = state.nameCharCount
        )
    }

    data class GivenNameData(
        val korean: String,
        val hanja: String,
        val charInfos: List<Map<String, String>>,
        val charCount: Int
    )

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

    fun forceUpdateUiState() {
        updateUiState()
    }
}