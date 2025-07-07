// model/domain/service/profile/ProfileEvaluationService.kt
package com.ssc.namespring.model.domain.service.profile

import android.util.Log
import com.ssc.namespring.model.domain.service.interfaces.EvaluationService
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.evaluation.SajuEvaluator
import com.ssc.namespring.model.domain.service.factory.NamingEngineProvider
import com.ssc.namespring.model.domain.service.profile.helpers.*
import com.ssc.namespring.utils.data.json.JsonLoader
import com.ssc.namingengine.NamingEngine

class ProfileEvaluationService(
    private val namingEngine: NamingEngine = NamingEngineProvider.getInstance()
) : EvaluationService {

    companion object {
        private const val TAG = "ProfileEvaluationService"
    }

    private val evaluationHelper = ProfileEvaluationHelper(namingEngine)
    private val updateHelper = ProfileUpdateHelper()
    private val validationHelper = ProfileValidationHelper()
    private val nameInputBuilder = NameInputBuilder()

    init {
        try {
            JsonLoader.scoreEvaluations // 초기화 체크
        } catch (e: Exception) {
            Log.e(TAG, "JsonLoader not initialized, attempting to initialize", e)
        }
    }

    override fun evaluate(profile: Profile): Profile {
        return if (validationHelper.hasCompleteName(profile)) {
            evaluateFullProfile(profile)
        } else {
            SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
        }
    }

    fun updateProfilesIfNeeded(profiles: List<Profile>): List<Profile> {
        val profilesToUpdate = profiles.filter { updateHelper.shouldUpdateProfile(it) }
        Log.d(TAG, "재평가 필요한 프로필: ${profilesToUpdate.size}개")

        return profiles.map { profile ->
            if (updateHelper.shouldUpdateProfile(profile)) {
                Log.d(TAG, "프로필 재평가 시작: ${profile.profileName}")
                val evaluated = evaluate(profile)
                Log.d(TAG, "프로필 재평가 완료: ${profile.profileName}")
                evaluated
            } else {
                profile
            }
        }
    }

    private fun evaluateFullProfile(profile: Profile): Profile {
        val givenName = profile.givenName ?: return profile
        val surname = profile.surname ?: return profile

        val nameInput = nameInputBuilder.buildNameInput(surname, givenName)
        Log.d(TAG, "=== evaluateFullProfile 시작 ===")
        Log.d(TAG, "이름 입력: $nameInput")

        val evaluatedProfile = evaluationHelper.evaluateFullProfile(profile, nameInput)
        return if (evaluatedProfile != null) {
            val generatedName = namingEngine.generateNames(
                userInput = nameInput,
                birthDateTime = profile.birthDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime(),
                useYajasi = profile.isYajaTime,
                verbose = true,
                withoutFilter = true
            ).firstOrNull()

            if (generatedName != null) {
                updateHelper.updateProfileWithGeneratedName(evaluatedProfile, givenName, generatedName)
            } else {
                evaluatedProfile
            }
        } else {
            SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
        }
    }

    fun needsEvaluation(profile: Profile): Boolean {
        return validationHelper.needsEvaluation(profile)
    }

    fun hasCompleteInfo(profile: Profile): Boolean {
        return validationHelper.hasCompleteInfo(profile)
    }

    fun evaluateIfNeeded(profile: Profile): Profile {
        return if (needsEvaluation(profile)) {
            evaluate(profile)
        } else {
            profile
        }
    }
}