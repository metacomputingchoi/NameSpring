// model/domain/usecase/ProfileManagerInstance.kt
package com.ssc.namespring.model.domain.usecase

import android.content.Context
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.interfaces.IProfileManager
import com.ssc.namespring.model.domain.service.profile.ProfileManagerImpl

/**
 * ProfileManager의 실제 구현을 담당하는 내부 클래스
 * 의존성 주입과 실제 비즈니스 로직을 분리
 */
internal class ProfileManagerInstance {

    private val implementation: IProfileManager

    /**
     * 프로덕션용 생성자 - Context를 받아 의존성 생성
     */
    constructor(context: Context) {
        val container = ProfileDependencyContainer(context)
        implementation = ProfileManagerImpl(container)
        implementation.init()
    }

    /**
     * 테스트용 생성자 - 구현체를 직접 주입
     */
    constructor(implementation: IProfileManager) {
        this.implementation = implementation
    }

    fun addProfile(profile: Profile): Boolean =
        implementation.addProfile(profile)

    fun updateProfile(profile: Profile): Boolean =
        implementation.updateProfile(profile)

    fun deleteProfiles(profileIds: List<String>) =
        implementation.deleteProfiles(profileIds)

    fun isDuplicateProfile(profile: Profile): Boolean =
        implementation.isDuplicateProfile(profile)

    fun searchProfiles(query: String): List<Profile> =
        implementation.searchProfiles(query)

    fun getSortedProfiles(sortType: ProfileManager.SortType): List<Profile> {
        val implSortType = when (sortType) {
            ProfileManager.SortType.NAME_ASC -> IProfileManager.SortType.NAME_ASC
            ProfileManager.SortType.NAME_DESC -> IProfileManager.SortType.NAME_DESC
            ProfileManager.SortType.SCORE_DESC -> IProfileManager.SortType.SCORE_DESC
            ProfileManager.SortType.SCORE_ASC -> IProfileManager.SortType.SCORE_ASC
            ProfileManager.SortType.DATE_DESC -> IProfileManager.SortType.DATE_DESC
            ProfileManager.SortType.DATE_ASC -> IProfileManager.SortType.DATE_ASC
        }
        return implementation.getSortedProfiles(implSortType)
    }

    fun getAllProfiles(): List<Profile> =
        implementation.getAllProfiles()

    fun getProfile(id: String): Profile? =
        implementation.getProfile(id)

    fun getCurrentProfile(): Profile? =
        implementation.getCurrentProfile()

    fun hasProfiles(): Boolean =
        implementation.hasProfiles()

    fun setSelectedProfile(profile: Profile) =
        implementation.setSelectedProfile(profile)

    fun getSelectedProfile(): Profile? =
        implementation.getSelectedProfile()

    fun switchProfile(id: String) =
        implementation.switchProfile(id)
}