// model/domain/usecase/ProfileAdapters.kt
package com.ssc.namespring.model.domain.usecase

import com.ssc.namespring.model.data.mapper.ProfileMigrator
import com.ssc.namespring.model.data.repository.ProfileRepository
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.interfaces.*
import com.ssc.namespring.model.domain.service.profile.ProfileEvaluationService
import com.ssc.namespring.model.domain.service.profile.ProfileServiceImpl

/**
 * 기존 구현체들을 인터페이스에 맞게 변환하는 어댑터들
 */

internal class ProfileRepositoryAdapter(
    private val repository: ProfileRepository
) : IProfileRepository {
    override fun saveProfiles(profiles: List<Profile>, currentProfileId: String?) {
        repository.saveProfiles(profiles, currentProfileId)
    }

    override fun loadProfiles(): List<Profile> = repository.loadProfiles()
    override fun loadProfilesJson(): String? = repository.loadProfilesJson()
    override fun loadCurrentProfileId(): String? = repository.loadCurrentProfileId()
    override fun clearProfiles() = repository.clearProfiles()
}

internal class ProfileServiceAdapter(
    private val service: ProfileServiceImpl
) : IProfileService {
    override fun initProfiles(profiles: List<Profile>, currentProfileId: String?) {
        service.initProfiles(profiles, currentProfileId)
    }

    override fun addProfile(profile: Profile): Boolean = service.addProfile(profile)
    override fun updateProfile(profile: Profile): Boolean = service.updateProfile(profile)
    override fun deleteProfiles(profileIds: List<String>) = service.deleteProfiles(profileIds)
    override fun getProfile(id: String): Profile? = service.getProfile(id)
    override fun getAllProfiles(): List<Profile> = service.getAllProfiles()
    override fun getCurrentProfile(): Profile? = service.getCurrentProfile()
    override fun switchProfile(id: String): Boolean = service.switchProfile(id)
    override fun searchProfiles(query: String): List<Profile> = service.searchProfiles(query)
    override fun getSortedProfiles(profileSortType: IProfileManager.ProfileSortType): List<Profile> =
        service.getSortedProfiles(profileSortType)
    override fun hasProfiles(): Boolean = service.hasProfiles()
    override fun setSelectedProfile(profile: Profile) = service.setSelectedProfile(profile)
    override fun getSelectedProfile(): Profile? = service.getSelectedProfile()
    override fun getCurrentProfileId(): String? = service.getCurrentProfileId()
    override fun replaceProfile(oldProfile: Profile, newProfile: Profile) =
        service.replaceProfile(oldProfile, newProfile)
}

internal class ProfileEvaluatorAdapter(
    private val evaluator: ProfileEvaluationService
) : IProfileEvaluator {
    override fun evaluate(profile: Profile): Profile = evaluator.evaluate(profile)
    override fun updateProfilesIfNeeded(profiles: List<Profile>): List<Profile> =
        evaluator.updateProfilesIfNeeded(profiles)
}

internal class ProfileMigratorAdapter(
    private val migrator: ProfileMigrator
) : IProfileMigrator {
    override fun migrateFromJson(json: String): List<Profile>? =
        migrator.migrateFromJson(json)
}