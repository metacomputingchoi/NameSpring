// model/domain/service/interfaces/EvaluationService.kt
package com.ssc.namespring.model.domain.service.interfaces

import com.ssc.namespring.model.domain.entity.Profile

interface EvaluationService {
    fun evaluate(profile: Profile): Profile
}
