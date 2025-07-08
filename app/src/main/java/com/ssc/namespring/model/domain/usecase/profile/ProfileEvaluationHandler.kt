// model/domain/usecase/profile/ProfileEvaluationHandler.kt
package com.ssc.namespring.model.domain.usecase.profile

import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.interfaces.IProfileService
import com.ssc.namespring.model.domain.service.interfaces.IProfileEvaluator

/**
 * 프로필 평가를 담당하는 클래스
 */
internal class ProfileEvaluationHandler(
    private val service: IProfileService,
    private val evaluator: IProfileEvaluator,
    private val persistenceHandler: ProfilePersistenceHandler
) {
    companion object {
        private const val TAG = "ProfileEvaluationHandler"
    }

    fun updateProfilesIfNeeded() {
        val profiles = service.getAllProfiles()

        val profilesToEvaluate = profiles.filter { profile ->
            profile.nameBomScore == 0 && hasCompleteInfo(profile)
        }

        Log.d(TAG, "평가 필요한 프로필: ${profilesToEvaluate.size}개")

        var hasChanges = false
        profilesToEvaluate.forEach { profile ->
            val evaluatedProfile = evaluator.evaluate(profile)
            service.replaceProfile(profile, evaluatedProfile)
            hasChanges = true
        }

        if (hasChanges) {
            persistenceHandler.saveProfiles()
            Log.d(TAG, "프로필 평가 완료 및 저장")
        }
    }

    private fun hasCompleteInfo(profile: Profile): Boolean {
        val hasCompleteName = profile.givenName?.let { givenName ->
            givenName.charInfos.isNotEmpty() &&
                    givenName.charInfos.all {
                        it.korean.isNotEmpty() && it.hanja.isNotEmpty()
                    }
        } == true

        return profile.surname != null && hasCompleteName
    }
}