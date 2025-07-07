// model/domain/service/profile/helpers/ProfileValidationHelper.kt
package com.ssc.namespring.model.domain.service.profile.helpers

import com.ssc.namespring.model.domain.entity.Profile

internal class ProfileValidationHelper {

    fun hasCompleteInfo(profile: Profile): Boolean {
        val hasCompleteName = profile.givenName?.let { givenName ->
            givenName.charInfos.isNotEmpty() &&
                    givenName.charInfos.all {
                        it.korean.isNotEmpty() && it.hanja.isNotEmpty()
                    }
        } == true

        return profile.surname != null && hasCompleteName
    }

    fun needsEvaluation(profile: Profile): Boolean {
        // 이미 평가된 프로필이고 정보가 완전하면 재평가 불필요
        if (profile.nameBomScore > 0 && profile.evaluatedNameJson != null) {
            return false
        }

        // 필수 정보가 모두 있는지 체크
        return hasCompleteInfo(profile)
    }

    fun hasCompleteName(profile: Profile): Boolean {
        return profile.givenName?.let { givenName ->
            givenName.charInfos.isNotEmpty() &&
                    givenName.charInfos.all { 
                        it.korean.isNotEmpty() && it.hanja.isNotEmpty() 
                    }
        } == true && profile.surname != null
    }
}