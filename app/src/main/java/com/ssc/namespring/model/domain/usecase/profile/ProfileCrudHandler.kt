// model/domain/usecase/profile/ProfileCrudHandler.kt
package com.ssc.namespring.model.domain.usecase.profile

import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.interfaces.IProfileService
import com.ssc.namespring.model.domain.service.interfaces.IProfileEvaluator

/**
 * 프로필 CRUD 작업을 담당하는 클래스
 */
internal class ProfileCrudHandler(
    private val service: IProfileService,
    private val evaluator: IProfileEvaluator,
    private val persistenceHandler: ProfilePersistenceHandler
) {
    companion object {
        private const val TAG = "ProfileCrudHandler"
    }

    fun addProfile(profile: Profile): Boolean {
        Log.d(TAG, "ProfileCrudHandler.addProfile 시작")
        Log.d(TAG, "  - 평가 전 evaluatedNameJson: ${profile.evaluatedNameJson?.length}")

        val evaluatedProfile = evaluator.evaluate(profile)

        Log.d(TAG, "  - 평가 후 evaluatedNameJson: ${evaluatedProfile.evaluatedNameJson?.length}")
        Log.d(TAG, "  - 평가 후 nameBomScore: ${evaluatedProfile.nameBomScore}")

        val success = service.addProfile(evaluatedProfile)
        if (success) persistenceHandler.saveProfiles()
        return success
    }

    fun updateProfile(profile: Profile): Boolean {
        Log.d(TAG, "ProfileCrudHandler.updateProfile 시작")
        Log.d(TAG, "  - 평가 전 evaluatedNameJson: ${profile.evaluatedNameJson?.length}")

        val evaluatedProfile = evaluator.evaluate(profile)

        Log.d(TAG, "  - 평가 후 evaluatedNameJson: ${evaluatedProfile.evaluatedNameJson?.length}")
        Log.d(TAG, "  - 평가 후 nameBomScore: ${evaluatedProfile.nameBomScore}")

        val success = service.updateProfile(evaluatedProfile)
        if (success) persistenceHandler.saveProfiles()
        return success
    }

    fun deleteProfiles(profileIds: List<String>) {
        service.deleteProfiles(profileIds)
        persistenceHandler.saveProfiles()
    }

    fun getProfile(id: String): Profile? = service.getProfile(id)

    fun getCurrentProfile(): Profile? = service.getCurrentProfile()

    fun getAllProfiles(): List<Profile> = service.getAllProfiles()

    fun setSelectedProfile(profile: Profile) = service.setSelectedProfile(profile)

    fun getSelectedProfile(): Profile? = service.getSelectedProfile()

    fun switchProfile(id: String) {
        if (service.switchProfile(id)) {
            persistenceHandler.saveProfiles()
        }
    }
}