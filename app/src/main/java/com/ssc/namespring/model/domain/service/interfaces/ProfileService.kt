// model/domain/service/interfaces/ProfileService.kt
package com.ssc.namespring.model.domain.service.interfaces

import com.ssc.namespring.model.domain.entity.Profile

interface ProfileService {
    fun addProfile(profile: Profile): Boolean
    fun updateProfile(profile: Profile): Boolean
    fun deleteProfiles(profileIds: List<String>)
    fun getProfile(id: String): Profile?
    fun getAllProfiles(): List<Profile>
    fun getCurrentProfile(): Profile?
    fun switchProfile(id: String): Boolean
}
