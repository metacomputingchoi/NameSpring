// model/domain/service/profileform/ProfileFormDataService.kt
package com.ssc.namespring.model.domain.service.profileform

import com.ssc.namespring.model.domain.builder.ProfileFormBuilder
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.usecase.NameDataManager
import com.ssc.namespring.model.domain.usecase.ProfileManager
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormDateTimeManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormStateManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormDataProvider
import com.ssc.namespring.model.presentation.components.ProfileFormUiState
import java.util.Calendar

class ProfileFormDataService(
    private val profileId: String?,
    private val dateTimeManager: ProfileFormDateTimeManager,
    private val nameDataManager: NameDataManager,
    private val stateManager: ProfileFormStateManager
) {
    private val profileBuilder = ProfileFormBuilder()
    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()
    private val dataProvider = ProfileFormDataProvider(nameDataManager, stateManager)

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

    fun getSelectedDate(): Calendar = dateTimeManager.getCalendar()
    fun getSurname(): SurnameInfo? = stateManager.getSurname()
    fun isYajaTime(): Boolean = stateManager.isYajaTime()
    fun getGivenNameInfo() = dataProvider.getGivenNameInfo()

    fun getGivenNameData(uiState: ProfileFormUiState) = 
        dataProvider.getGivenNameData(uiState)
}