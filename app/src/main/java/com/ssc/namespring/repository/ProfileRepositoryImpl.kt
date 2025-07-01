// repository/ProfileRepositoryImpl.kt
package com.ssc.namespring.repository

import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.repository.ProfileRepository
import kotlinx.coroutines.delay

/**
 * 메모리 기반 프로필 저장소 구현
 * 실제 앱에서는 Room Database로 교체
 */
class ProfileRepositoryImpl : ProfileRepository {

    private val profiles = mutableMapOf<String, Profile>()
    private var defaultProfileId: String? = null

    override suspend fun insert(profile: Profile) {
        delay(10) // DB 작업 시뮬레이션
        profiles[profile.id] = profile

        // 첫 프로필은 기본으로 설정
        if (profiles.size == 1) {
            defaultProfileId = profile.id
        }
    }

    override suspend fun update(profile: Profile) {
        delay(10)
        profiles[profile.id] = profile
    }

    override suspend fun delete(id: String) {
        delay(10)
        profiles.remove(id)

        // 기본 프로필이 삭제되면 다른 프로필을 기본으로
        if (defaultProfileId == id) {
            defaultProfileId = profiles.keys.firstOrNull()
        }
    }

    override suspend fun getById(id: String): Profile? {
        delay(5)
        return profiles[id]
    }

    override suspend fun getAll(): List<Profile> {
        delay(5)
        return profiles.values.toList()
    }

    override suspend fun getByName(surname: String, givenName: String): Profile? {
        delay(5)
        return profiles.values.find { 
            it.surname == surname && it.givenName == givenName 
        }
    }

    override suspend fun getDefaultProfile(): Profile? {
        delay(5)
        return defaultProfileId?.let { profiles[it] }
    }

    override suspend fun setDefaultProfile(id: String) {
        delay(5)
        if (profiles.containsKey(id)) {
            defaultProfileId = id
        }
    }
}