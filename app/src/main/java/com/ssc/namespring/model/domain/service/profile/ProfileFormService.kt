// model/domain/service/profile/ProfileFormService.kt
package com.ssc.namespring.model.domain.service.profile

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.ssc.namespring.model.domain.entity.Profile
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
            val profile = formManager.createProfile(profileName)

            // 기존 프로필과 비교하여 변경사항 체크
            val existingProfile = profileId?.let { profileManager.getProfile(it) }
            val hasChanges = hasSignificantChanges(existingProfile, profile)

            // 평가 수행 여부 결정
            val evaluatedProfile = if (hasChanges && hasCompleteInfo(profile)) {
                val evaluationService = ProfileEvaluationService(NamingEngine.create())
                evaluationService.evaluate(profile)
            } else if (existingProfile != null && !hasChanges) {
                // 변경사항이 없으면 기존 평가 정보 유지
                profile.copy(
                    nameBomScore = existingProfile.nameBomScore,
                    evaluatedNameJson = existingProfile.evaluatedNameJson,
                    sajuInfo = existingProfile.sajuInfo,
                    ohaengInfo = existingProfile.ohaengInfo
                )
            } else {
                profile
            }

            val success = if (!profileId.isNullOrEmpty()) {
                profileManager.updateProfile(evaluatedProfile)
            } else {
                profileManager.addProfile(evaluatedProfile)
            }

            callback(success)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile", e)
            callback(false)
        }
    }

    // 유의미한 변경사항 체크 메서드 추가
    private fun hasSignificantChanges(existing: Profile?, new: Profile): Boolean {
        if (existing == null) return true

        // 이름 변경 체크
        if (existing.surname?.korean != new.surname?.korean ||
            existing.surname?.hanja != new.surname?.hanja) return true

        // 이름 글자 수 변경 체크
        val existingCharCount = existing.givenName?.charInfos?.size ?: 0
        val newCharCount = new.givenName?.charInfos?.size ?: 0
        if (existingCharCount != newCharCount) return true

        // 각 글자의 한글/한자 변경 체크
        existing.givenName?.charInfos?.forEachIndexed { index, charInfo ->
            val newCharInfo = new.givenName?.charInfos?.getOrNull(index)
            if (charInfo.korean != newCharInfo?.korean ||
                charInfo.hanja != newCharInfo?.hanja) return true
        }

        // 생년월일시 변경 체크
        if (existing.birthDate.timeInMillis != new.birthDate.timeInMillis ||
            existing.isYajaTime != new.isYajaTime) return true

        return false
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