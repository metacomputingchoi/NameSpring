// model/domain/usecase/profile/ProfileMigrationHandler.kt
package com.ssc.namespring.model.domain.usecase.profile

import android.util.Log
import com.ssc.namespring.model.domain.service.interfaces.IProfileMigrator
import com.ssc.namespring.model.domain.service.interfaces.IProfileRepository
import com.ssc.namespring.model.domain.service.interfaces.IProfileService

/**
 * 프로필 마이그레이션을 담당하는 클래스
 */
internal class ProfileMigrationHandler(
    private val migrator: IProfileMigrator,
    private val repository: IProfileRepository,
    private val service: IProfileService
) {
    companion object {
        private const val TAG = "ProfileMigrationHandler"
    }

    fun migrateFromJson(json: String): Boolean {
        val migratedProfiles = migrator.migrateFromJson(json)
        return if (migratedProfiles != null) {
            service.initProfiles(migratedProfiles, null)
            true
        } else {
            repository.clearProfiles()
            service.initProfiles(emptyList(), null)
            false
        }
    }
}