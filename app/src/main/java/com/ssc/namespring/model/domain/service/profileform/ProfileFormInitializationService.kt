// model/domain/service/profileform/ProfileFormInitializationService.kt
package com.ssc.namespring.model.domain.service.profileform

import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.loader.ProfileFormLoader
import com.ssc.namespring.model.domain.usecase.NameDataManager
import com.ssc.namespring.model.domain.usecase.ProfileManager
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormDateTimeManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormStateManager
import com.ssc.namespring.model.domain.service.factory.NameDataServiceFactory
import com.ssc.namespring.model.domain.service.interfaces.INameDataService

class ProfileFormInitializationService(
    private val profileId: String?,
    private val dateTimeManager: ProfileFormDateTimeManager,
    private val nameDataManager: NameDataManager,
    private val stateManager: ProfileFormStateManager,
    private val nameDataService: INameDataService,
    private val onStateUpdate: () -> Unit
) {
    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()
    private val profileLoader = ProfileFormLoader(
        dateTimeManager, nameDataManager, stateManager, nameDataService
    )

    fun initialize() {
        if (!profileId.isNullOrEmpty()) {
            profileManager.getProfile(profileId)?.let { profile ->
                profileLoader.loadProfileData(profile)
                onStateUpdate()
            }
        } else {
            nameDataManager.initialize()
            onStateUpdate()
        }
    }

    fun loadFromParentProfile(parentProfile: Profile): Boolean {
        return if (profileLoader.loadFromParentProfile(parentProfile)) {
            onStateUpdate()
            true
        } else {
            false
        }
    }
}