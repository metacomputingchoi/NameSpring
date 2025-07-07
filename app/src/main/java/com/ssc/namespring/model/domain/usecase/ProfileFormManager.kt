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
import com.ssc.namespring.model.domain.validation.ProfileFormValidationHelper
import com.ssc.namespring.model.domain.helper.NameDataHandler
import com.ssc.namespring.model.domain.builder.ProfileFormBuilder
import com.ssc.namespring.model.domain.coordinator.ProfileFormUiCoordinator
import com.ssc.namespring.model.domain.loader.ProfileFormLoader
import com.ssc.namingengine.NamingEngine
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

/**
 * ProfileForm 관리를 담당하는 메인 클래스
 * 내부적으로 여러 헬퍼 클래스들에게 책임을 위임하여 단일 책임 원칙을 준수
 */
class ProfileFormManager(private val profileId: String? = null) {
    companion object {
        private const val TAG = "ProfileFormManager"
    }

    // Core Managers
    private val dateTimeManager = ProfileFormDateTimeManager()
    private val nameDataManager = NameDataManager()
    private val stateManager = ProfileFormStateManager()
    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()
    private val nameDataService: INameDataService = NameDataServiceFactory.getInstance()

    // Helper Classes
    private val validationHelper = ProfileFormValidationHelper()
    private val nameDataHandler = NameDataHandler(nameDataManager, nameDataService)
    private val profileBuilder = ProfileFormBuilder()
    private val uiCoordinator = ProfileFormUiCoordinator(dateTimeManager, nameDataManager, stateManager)
    private val profileLoader = ProfileFormLoader(dateTimeManager, nameDataManager, stateManager, nameDataService)

    // LiveData delegates
    val uiState: LiveData<ProfileFormUiState> = uiCoordinator.uiState
    val profileLoaded: LiveData<Boolean> = uiCoordinator.profileLoaded

    init {
        nameDataManager.initialize()
        updateUiState()
    }

    fun initialize() {
        if (!profileId.isNullOrEmpty()) {
            profileManager.getProfile(profileId)?.let { profile ->
                profileLoader.loadProfileData(profile)
                updateUiState()
            }
        } else {
            nameDataManager.initialize()
            updateUiState()
        }
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
        uiCoordinator.resetProfileLoadedFlag()
    }

    fun addNameChar() {
        if (nameDataHandler.addCharIfPossible()) {
            updateUiState()
        }
    }

    fun removeNameChar() {
        if (nameDataHandler.removeCharIfPossible()) {
            updateUiState()
        }
    }

    fun setSurname(surname: SurnameInfo?) {
        stateManager.setSurname(surname)
        updateUiState()
    }

    fun setHanjaInfo(position: Int, korean: String, hanja: String) {
        if (nameDataHandler.updateHanjaInfo(position, korean, hanja)) {
            updateUiState()
        }
    }

    fun resetAllFields() {
        dateTimeManager.reset()
        nameDataManager.reset()
        stateManager.reset()
        updateUiState()
    }

    fun syncWithUiValues(containerView: LinearLayout) {
        nameDataHandler.syncWithUI(containerView)
        updateUiState()
    }

    fun createProfile(profileName: String): Profile {
        return profileBuilder.buildProfile(
            profileId = profileId,
            profileName = profileName,
            birthDate = dateTimeManager.getCalendar(),
            isYajaTime = stateManager.isYajaTime(),
            surname = stateManager.getSurname(),
            givenNameInfo = nameDataManager.createGivenNameInfo(),
            existingProfile = if (!profileId.isNullOrEmpty())
                profileManager.getProfile(profileId) else null
        )
    }

    fun getNameDataManager(): INameDataManager = nameDataManager
    fun getSelectedDate() = dateTimeManager.getCalendar()

    fun isValidForNaming(): Boolean {
        val result = validationHelper.validateForNaming(
            surname = getSurname(),
            nameCharCount = nameDataManager.getCharCount()
        )
        return result.isValid
    }

    fun isValidForEvaluation(): Boolean {
        val result = validationHelper.validateForEvaluation(
            surname = getSurname(),
            givenNameInfo = getGivenNameInfo()
        )
        return result.isValid
    }

    fun createNamingInput(): NamingEngineInput? {
        val surname = getSurname() ?: return null
        val currentUiState = uiState.value ?: return null

        return profileBuilder.buildNamingInput(
            surname = surname,
            uiState = currentUiState,
            calendar = dateTimeManager.getCalendar(),
            isYajaTime = isYajaTime()
        )
    }

    fun createEvaluationInput(): NamingEngineInput? {
        return createNamingEngineInput()
    }

    fun loadFromParentProfile(parentProfile: Profile) {
        if (profileLoader.loadFromParentProfile(parentProfile)) {
            updateUiState()
            uiCoordinator.setProfileLoaded(true)
        }
    }

    fun getSurname(): SurnameInfo? = stateManager.getSurname()
    fun isYajaTime(): Boolean = stateManager.isYajaTime()

    fun createNamingEngineInput(): NamingEngineInput? {
        val surname = getSurname() ?: return null
        val givenNameInfo = nameDataManager.createGivenNameInfo()

        return profileBuilder.buildEvaluationInput(
            surname = surname,
            givenNameInfo = givenNameInfo,
            calendar = dateTimeManager.getCalendar(),
            isYajaTime = isYajaTime()
        )
    }

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

    data class NamingEngineInput(
        val userInput: String,
        val birthDateTime: LocalDateTime,
        val useYajasi: Boolean
    )

    private fun updateUiState() {
        uiCoordinator.updateState()
    }

    fun forceUpdateUiState() {
        updateUiState()
    }
}
