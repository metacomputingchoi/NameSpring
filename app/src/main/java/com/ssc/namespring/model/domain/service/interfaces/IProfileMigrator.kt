// model/domain/service/interfaces/IProfileMigrator.kt
package com.ssc.namespring.model.domain.service.interfaces

import com.ssc.namespring.model.domain.entity.Profile

interface IProfileMigrator {
    fun migrateFromJson(json: String): List<Profile>?
}