// model/domain/usecase/profile/ProfilePersistenceHandler.kt
package com.ssc.namespring.model.domain.usecase.profile

import com.ssc.namespring.model.domain.service.interfaces.IProfileRepository
import com.ssc.namespring.model.domain.service.interfaces.IProfileService

/**
 * 프로필 저장을 담당하는 클래스
 */
internal class ProfilePersistenceHandler(
    private val repository: IProfileRepository,
    private val service: IProfileService
) {
    fun saveProfiles() {
        repository.saveProfiles(service.getAllProfiles(), service.getCurrentProfileId())
    }
}