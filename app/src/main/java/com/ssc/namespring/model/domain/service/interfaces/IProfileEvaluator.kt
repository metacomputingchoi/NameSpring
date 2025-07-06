// model/domain/service/interfaces/IProfileEvaluator.kt
package com.ssc.namespring.model.domain.service.interfaces

import com.ssc.namespring.model.domain.entity.Profile

interface IProfileEvaluator {
    fun evaluate(profile: Profile): Profile
    fun updateProfilesIfNeeded(profiles: List<Profile>): List<Profile>
}