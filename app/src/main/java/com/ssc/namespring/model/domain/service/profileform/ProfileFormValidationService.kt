// model/domain/service/profileform/ProfileFormValidationService.kt
package com.ssc.namespring.model.domain.service.profileform

import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.usecase.NameDataManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormValidator
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormStateManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormDataProvider
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormInputFactory
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormDateTimeManager
import com.ssc.namespring.model.domain.usecase.profileform.NamingEngineInput
import com.ssc.namespring.model.presentation.components.ProfileFormUiState

class ProfileFormValidationService(
    private val dateTimeManager: ProfileFormDateTimeManager,
    private val nameDataManager: NameDataManager,
    private val stateManager: ProfileFormStateManager
) {
    private val validator = ProfileFormValidator()
    private val inputFactory = ProfileFormInputFactory()
    private val dataProvider = ProfileFormDataProvider(nameDataManager, stateManager)

    fun isValidForNaming(): Boolean = 
        validator.isValidForNaming(stateManager.getSurname(), nameDataManager.getCharCount())

    fun isValidForEvaluation(): Boolean = 
        validator.isValidForEvaluation(stateManager.getSurname(), dataProvider.getGivenNameInfo())

    fun createNamingInput(uiState: ProfileFormUiState): NamingEngineInput? {
        val surname = stateManager.getSurname() ?: return null
        return inputFactory.createNamingInput(
            surname, uiState, dateTimeManager.getCalendar(), stateManager.isYajaTime()
        )
    }

    fun createEvaluationInput(): NamingEngineInput? {
        val surname = stateManager.getSurname() ?: return null
        val givenNameInfo = nameDataManager.createGivenNameInfo()
        return inputFactory.createEvaluationInput(
            surname, givenNameInfo, dateTimeManager.getCalendar(), stateManager.isYajaTime()
        )
    }
}