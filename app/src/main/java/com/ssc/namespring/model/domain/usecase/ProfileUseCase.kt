// model/domain/usecase/ProfileUseCase.kt
package com.ssc.namespring.model.domain.usecase

import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.interfaces.IProfileManager
import com.ssc.namespring.model.domain.service.interfaces.IProfileRepository
import com.ssc.namespring.model.domain.service.interfaces.IProfileService
import com.ssc.namespring.model.domain.service.interfaces.IProfileEvaluator
import com.ssc.namespring.model.domain.service.interfaces.IProfileMigrator
import com.ssc.namespring.model.domain.usecase.profile.*

/**
 * 프로필 관련 비즈니스 로직을 담당하는 UseCase
 * Facade 패턴을 사용하여 내부 핸들러들을 조율
 */
class ProfileUseCase(
    repository: IProfileRepository,
    service: IProfileService,
    evaluator: IProfileEvaluator,
    migrator: IProfileMigrator
) {
    private val persistenceHandler = ProfilePersistenceHandler(repository, service)
    private val evaluationHandler = ProfileEvaluationHandler(service, evaluator, persistenceHandler)
    private val initializer = ProfileInitializer(repository, service, migrator, evaluationHandler)
    private val crudHandler = ProfileCrudHandler(service, evaluator, persistenceHandler)
    private val searchHandler = ProfileSearchHandler(service)

    fun initialize() = initializer.initialize()

    fun addProfile(profile: Profile): Boolean = crudHandler.addProfile(profile)

    fun updateProfile(profile: Profile): Boolean = crudHandler.updateProfile(profile)

    fun deleteProfiles(profileIds: List<String>) = crudHandler.deleteProfiles(profileIds)

    fun isDuplicateProfile(profile: Profile): Boolean = searchHandler.isDuplicateProfile(profile)

    fun searchProfiles(query: String): List<Profile> = searchHandler.searchProfiles(query)

    fun getSortedProfiles(profileSortType: IProfileManager.ProfileSortType): List<Profile> =
        searchHandler.getSortedProfiles(profileSortType)

    fun getAllProfiles(): List<Profile> = crudHandler.getAllProfiles()

    fun getProfile(id: String): Profile? = crudHandler.getProfile(id)

    fun getCurrentProfile(): Profile? = crudHandler.getCurrentProfile()

    fun hasProfiles(): Boolean = searchHandler.hasProfiles()

    fun setSelectedProfile(profile: Profile) = crudHandler.setSelectedProfile(profile)

    fun getSelectedProfile(): Profile? = crudHandler.getSelectedProfile()

    fun switchProfile(id: String) = crudHandler.switchProfile(id)
}