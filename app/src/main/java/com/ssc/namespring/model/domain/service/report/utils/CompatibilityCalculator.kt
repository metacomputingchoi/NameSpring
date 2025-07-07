// model/domain/service/report/utils/CompatibilityCalculator.kt
package com.ssc.namespring.model.domain.service.report.utils

import com.ssc.namespring.model.domain.entity.Profile

class CompatibilityCalculator {

    fun calculateNameCompatibility(profile: Profile): String {
        return when {
            profile.surname != null && profile.givenName != null -> "양호"
            else -> "평가 불가"
        }
    }

    fun calculateBirthDateCompatibility(profile: Profile): String {
        return when {
            profile.sajuInfo != null -> "분석 완료"
            else -> "사주 정보 필요"
        }
    }
}