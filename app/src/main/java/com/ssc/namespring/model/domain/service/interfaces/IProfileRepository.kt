// model/domain/service/interfaces/IProfileRepository.kt
package com.ssc.namespring.model.domain.service.interfaces

import com.ssc.namespring.model.domain.entity.Profile

interface IProfileRepository {
    fun saveProfiles(profiles: List<Profile>, currentProfileId: String?)
    fun loadProfiles(): List<Profile>
    fun loadProfilesJson(): String?
    fun loadCurrentProfileId(): String?
    fun clearProfiles()
}