// ui/profileform/ProfileFormCoordinator.kt
package com.ssc.namespring.ui.profileform

import android.app.Activity
import android.widget.Toast
import com.google.gson.Gson
import com.ssc.namespring.ProfileFormActivity
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.ProfileFormMode
import com.ssc.namespring.model.domain.usecase.ProfileManager
import kotlinx.coroutines.delay

class ProfileFormCoordinator(
    private val activity: ProfileFormActivity,
    private val profileManager: ProfileManager
) {
    suspend fun loadTempProfileIfExists(formManager: com.ssc.namespring.model.domain.usecase.ProfileFormManager) {
        val prefs = activity.getSharedPreferences("temp_profile", Activity.MODE_PRIVATE)
        val profileData = prefs.getString("profile_data", null)
        if (profileData != null) {
            val profile = Gson().fromJson(profileData, Profile::class.java)
            prefs.edit().clear().apply()

            delay(500)  // UI 초기화 대기
            formManager.loadFromParentProfile(profile)
        }
    }

    suspend fun loadProfileForEdit(
        profileId: String,
        formManager: com.ssc.namespring.model.domain.usecase.ProfileFormManager,
        uiComponents: ProfileFormUIComponents
    ) {
        profileManager.getProfile(profileId)?.let { profile ->
            // 프로필 이름 설정 - profileName 사용
            uiComponents.etProfileName.setText(profile.profileName)

            // 프로필 데이터 로드
            formManager.loadFromParentProfile(profile)

            // UI 업데이트 강제 실행
            delay(100)
            formManager.forceUpdateUiState()
        } ?: run {
            Toast.makeText(activity, "프로필을 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
            activity.finish()
        }
    }

    fun loadParentProfileData(
        parentProfileId: String?,
        formManager: com.ssc.namespring.model.domain.usecase.ProfileFormManager,
        nameInputHandler: ProfileFormNameInputHandler,
        uiComponents: ProfileFormUIComponents
    ) {
        parentProfileId?.let { parentId ->
            profileManager.getProfile(parentId)?.let { parentProfile ->
                nameInputHandler.cleanup()
                formManager.loadFromParentProfile(parentProfile)

                uiComponents.nameInputContainer.postDelayed({
                    formManager.forceUpdateUiState()
                    Toast.makeText(activity, "프로필 데이터를 불러왔습니다", Toast.LENGTH_SHORT).show()
                }, 100)
            }
        }
    }

    fun validateAndGetProfileName(
        uiComponents: ProfileFormUIComponents,
        config: com.ssc.namespring.model.domain.entity.ProfileFormConfig
    ): String? {
        val profileName = uiComponents.etProfileName.text?.toString()?.takeIf { it.isNotEmpty() }
            ?: if (config.mode in listOf(ProfileFormMode.NAMING, ProfileFormMode.EVALUATION)) {
                config.getDefaultName()
            } else {
                ""
            }

        if (profileName.isEmpty()) {
            Toast.makeText(
                activity,
                "${config.profileLabelText}을(를) 입력해주세요",
                Toast.LENGTH_SHORT
            ).show()
            return null
        }

        return profileName
    }
}