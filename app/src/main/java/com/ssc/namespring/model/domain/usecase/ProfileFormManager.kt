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
import com.ssc.namespring.model.domain.coordinator.ProfileFormUiCoordinator
import com.ssc.namespring.model.domain.service.profileform.*
import java.util.Calendar

/**
 * ProfileForm 관리를 담당하는 메인 클래스
 * 내부적으로 여러 서비스들에게 책임을 위임하여 단일 책임 원칙을 준수
 */
class ProfileFormManager(private val profileId: String? = null) {
    companion object {
        private const val TAG = "ProfileFormManager"
    }

    // Core Managers
    private val dateTimeManager = ProfileFormDateTimeManager()
    private val nameDataManager = NameDataManager()
    private val stateManager = ProfileFormStateManager()
    private val nameDataService: INameDataService = NameDataServiceFactory.getInstance()

    // UI Coordinator
    private val uiCoordinator = ProfileFormUiCoordinator(
        dateTimeManager, nameDataManager, stateManager
    )

    // Services
    private val initializationService = ProfileFormInitializationService(
        profileId, dateTimeManager, nameDataManager, stateManager, 
        nameDataService, ::updateUiState
    )

    private val operationService = ProfileFormOperationService(
        dateTimeManager, nameDataManager, stateManager, nameDataService, ::updateUiState
    )

    private val dataService = ProfileFormDataService(
        profileId, dateTimeManager, nameDataManager, stateManager
    )

    private val validationService = ProfileFormValidationService(
        dateTimeManager, nameDataManager, stateManager
    )

    // LiveData delegates
    val uiState: LiveData<ProfileFormUiState> = uiCoordinator.uiState
    val profileLoaded: LiveData<Boolean> = uiCoordinator.profileLoaded

    init {
        nameDataManager.initialize()
        updateUiState()
    }

    // Initialization
    fun initialize() = initializationService.initialize()
    fun loadFromParentProfile(parentProfile: Profile) {
        if (initializationService.loadFromParentProfile(parentProfile)) {
            uiCoordinator.setProfileLoaded(true)
        }
    }

    // Operations
    fun updateDate(calendar: Calendar) = operationService.updateDate(calendar)
    fun updateTime(calendar: Calendar) = operationService.updateTime(calendar)
    fun updateYajaTime(isChecked: Boolean) = operationService.updateYajaTime(isChecked)
    fun setSurname(surname: SurnameInfo?) = operationService.setSurname(surname)
    fun addNameChar() = operationService.addNameChar()
    fun removeNameChar() = operationService.removeNameChar()
    fun setHanjaInfo(position: Int, korean: String, hanja: String) = 
        operationService.setHanjaInfo(position, korean, hanja)
    fun syncWithUiValues(containerView: LinearLayout) = 
        operationService.syncWithUiValues(containerView)
    fun resetAllFields() = operationService.resetAllFields()

    // Data Access
    fun createProfile(profileName: String): Profile = dataService.createProfile(profileName)
    fun getSelectedDate() = dataService.getSelectedDate()
    fun getSurname() = dataService.getSurname()
    fun isYajaTime() = dataService.isYajaTime()
    fun getGivenNameInfo() = dataService.getGivenNameInfo()
    fun getGivenNameData() = dataService.getGivenNameData(uiState.value ?: ProfileFormUiState())
    fun getNameDataManager(): INameDataManager = nameDataManager

    // Validation
    fun isValidForNaming() = validationService.isValidForNaming()
    fun isValidForEvaluation() = validationService.isValidForEvaluation()
    fun createNamingInput() = validationService.createNamingInput(uiState.value ?: ProfileFormUiState())
    fun createEvaluationInput() = validationService.createEvaluationInput()
    fun createNamingEngineInput() = validationService.createEvaluationInput()

    // UI
    fun resetProfileLoadedFlag() = uiCoordinator.resetProfileLoadedFlag()
    private fun updateUiState() = uiCoordinator.updateState()
    fun forceUpdateUiState() = updateUiState()
}