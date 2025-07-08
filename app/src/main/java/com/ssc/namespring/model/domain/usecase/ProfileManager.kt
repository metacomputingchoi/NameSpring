// model/domain/usecase/ProfileManager.kt
package com.ssc.namespring.model.domain.usecase

import com.ssc.namespring.model.domain.entity.Profile

/**
 * ProfileManager - 프로필 관리 기능을 제공하는 인터페이스
 *
 * 의존성 주입을 통해 사용하도록 설계
 */
interface ProfileManager {

    enum class ProfileManagerSortType {
        NAME_ASC, NAME_DESC, SCORE_DESC, SCORE_ASC, DATE_DESC, DATE_ASC
    }

    fun addProfile(profile: Profile): Boolean
    fun updateProfile(profile: Profile): Boolean
    fun deleteProfiles(profileIds: List<String>)
    fun deleteProfile(profileId: String)
    fun isDuplicateProfile(profile: Profile): Boolean
    fun searchProfiles(query: String): List<Profile>
    fun getSortedProfiles(profileManagerSortType: ProfileManagerSortType): List<Profile>
    fun getAllProfiles(): List<Profile>
    fun getProfile(id: String): Profile?
    fun getCurrentProfile(): Profile?
    fun hasProfiles(): Boolean
    fun setSelectedProfile(profile: Profile)
    fun getSelectedProfile(): Profile?
    fun switchProfile(id: String)
}