// model/domain/service/profile/ProfileFormService.kt
package com.ssc.namespring.model.domain.service.profile

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.ssc.namespring.model.domain.usecase.ProfileFormManager
import com.ssc.namespring.model.domain.usecase.ProfileManager
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namingengine.NamingEngine

class ProfileFormService {
    companion object {
        private const val TAG = "ProfileFormService"
    }

    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()

    fun saveProfile(
        context: Context,
        formManager: ProfileFormManager,
        profileName: String,
        profileId: String?,
        callback: (Boolean) -> Unit
    ) {
        try {
            Log.d(TAG, "saveProfile: profileName='$profileName', profileId=$profileId")

            val profile = formManager.createProfile(profileName)

            // 프로필 데이터 검증 로그
            Log.d(TAG, "Created profile:")
            Log.d(TAG, "  - Name: ${profile.profileName}")
            Log.d(TAG, "  - Surname: ${profile.surname?.korean}(${profile.surname?.hanja})")
            Log.d(TAG, "  - GivenName: ${profile.givenName?.korean}(${profile.givenName?.hanja})")
            profile.givenName?.charInfos?.forEachIndexed { index, charInfo ->
                Log.d(TAG, "    CharInfo[$index]: ${charInfo.korean}/${charInfo.hanja}")
            }

            // 프로필 저장 전에 평가 수행
            val evaluatedProfile = if (profile.surname != null && profile.givenName != null) {
                // ProfileEvaluationService를 통해 평가
                val evaluationService = ProfileEvaluationService(NamingEngine.create())
                val evaluated = evaluationService.evaluate(profile)

                // 평가 결과 로그
                Log.d(TAG, "After evaluation:")
                Log.d(TAG, "  - evaluatedName exists: ${evaluated.evaluatedName != null}")
                Log.d(TAG, "  - evaluatedNameJson length: ${evaluated.evaluatedNameJson?.length}")
                Log.d(TAG, "  - nameBomScore: ${evaluated.nameBomScore}")

                // 디버깅을 위해 evaluatedName의 내용도 로그
                evaluated.evaluatedName?.let { name ->
                    Log.d(TAG, "  - GeneratedName details:")
                    Log.d(TAG, "    - combinedHanja: ${name.combinedHanja}")
                    Log.d(TAG, "    - combinedPronounciation: ${name.combinedPronounciation}")
                    Log.d(TAG, "    - hanjaDetails count: ${name.hanjaDetails.size}")
                    Log.d(TAG, "    - analysisInfo exists: ${name.analysisInfo != null}")
                }

                evaluated
            } else {
                // 이름 정보가 불완전한 경우 evaluatedName = null로 유지
                Log.d(TAG, "이름 정보가 불완전하여 평가하지 않음")
                profile
            }

            val success = if (!profileId.isNullOrEmpty()) {
                profileManager.updateProfile(evaluatedProfile)
            } else {
                profileManager.addProfile(evaluatedProfile)
            }

            if (!success) {
                showDuplicateProfileDialog(context)
            }

            callback(success)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile", e)
            e.printStackTrace()
            callback(false)
        }
    }

    fun showResetDialog(context: Context, onConfirm: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("전체 초기화")
            .setMessage("입력한 모든 정보가 초기화됩니다. 계속하시겠습니까?")
            .setPositiveButton("초기화") { _, _ -> onConfirm() }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showDuplicateProfileDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("중복된 프로필")
            .setMessage("동일한 프로필이 이미 존재합니다.\n(프로필명, 생년월일시분, 성씨가 모두 동일)")
            .setPositiveButton("확인", null)
            .show()
    }
}