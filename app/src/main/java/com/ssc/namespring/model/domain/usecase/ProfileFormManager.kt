// model/domain/usecase/ProfileFormManager.kt
package com.ssc.namespring.model.domain.usecase

import android.widget.LinearLayout
import androidx.lifecycle.LiveData
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.presentation.components.ProfileFormUiState
import com.ssc.namespring.model.domain.usecase.profileform.*
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import com.ssc.namespring.model.domain.service.factory.NameDataServiceFactory
import com.ssc.namespring.model.domain.helper.NameDataHandler
import com.ssc.namespring.model.domain.builder.ProfileFormBuilder
import com.ssc.namespring.model.domain.coordinator.ProfileFormUiCoordinator
import com.ssc.namespring.model.domain.loader.ProfileFormLoader
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
    private val nameDataHandler = NameDataHandler(nameDataManager, nameDataService)
    private val profileBuilder = ProfileFormBuilder()
    private val uiCoordinator = ProfileFormUiCoordinator(dateTimeManager, nameDataManager, stateManager)
    private val profileLoader = ProfileFormLoader(dateTimeManager, nameDataManager, stateManager, nameDataService)

    // Delegate Classes
    private val validator = ProfileFormValidator()
    private val inputFactory = ProfileFormInputFactory()
    private val dataProvider = ProfileFormDataProvider(nameDataManager, stateManager)

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

    fun resetProfileLoadedFlag() = uiCoordinator.resetProfileLoadedFlag()

    fun addNameChar() {
        if (nameDataHandler.addCharIfPossible()) updateUiState()
    }

    fun removeNameChar() {
        if (nameDataHandler.removeCharIfPossible()) updateUiState()
    }

    fun setSurname(surname: SurnameInfo?) {
        stateManager.setSurname(surname)
        updateUiState()
    }

    fun setHanjaInfo(position: Int, korean: String, hanja: String) {
        if (nameDataHandler.updateHanjaInfo(position, korean, hanja)) updateUiState()
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
    fun getSurname(): SurnameInfo? = stateManager.getSurname()
    fun isYajaTime(): Boolean = stateManager.isYajaTime()
    fun getGivenNameInfo() = dataProvider.getGivenNameInfo()

    fun isValidForNaming(): Boolean = 
        validator.isValidForNaming(getSurname(), nameDataManager.getCharCount())

    fun isValidForEvaluation(): Boolean = 
        validator.isValidForEvaluation(getSurname(), getGivenNameInfo())

    fun createNamingInput(): NamingEngineInput? {
        val surname = getSurname() ?: return null
        val currentUiState = uiState.value ?: return null
        return inputFactory.createNamingInput(surname, currentUiState, 
            dateTimeManager.getCalendar(), isYajaTime())
    }

    fun createEvaluationInput() = createNamingEngineInput()

    fun createNamingEngineInput(): NamingEngineInput? {
        val surname = getSurname() ?: return null
        val givenNameInfo = nameDataManager.createGivenNameInfo()
        return inputFactory.createEvaluationInput(surname, givenNameInfo,
            dateTimeManager.getCalendar(), isYajaTime())
    }

    fun loadFromParentProfile(parentProfile: Profile) {
        if (profileLoader.loadFromParentProfile(parentProfile)) {
            updateUiState()
            uiCoordinator.setProfileLoaded(true)
        }
    }

    fun getGivenNameData() = dataProvider.getGivenNameData(uiState.value ?: ProfileFormUiState())

    private fun updateUiState() = uiCoordinator.updateState()
    fun forceUpdateUiState() = updateUiState()
}