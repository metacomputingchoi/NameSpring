// controller/ProfileController.kt
package com.ssc.namespring.controller

import com.ssc.namespring.model.ProfileModel
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.ProfileInputView
import com.ssc.namespring.view.ProfileManagementView
import com.ssc.namespring.view.impl.ProfileInputViewImpl
import java.time.LocalDateTime

class ProfileController(
    private val profileModel: ProfileModel,
    private val profileManagementView: ProfileManagementView,
    private val profileInputView: ProfileInputView
) {

    private val logger = AndroidLogger("ProfileController")

    // 프로필 생성 완료 콜백 추가
    var onProfileCreated: ((Profile) -> Unit)? = null

    suspend fun showProfileManagement(): Boolean {  // 반환 타입 변경
        profileManagementView.showLoading(true)

        try {
            val profiles = profileModel.getAllProfiles().getOrThrow()
            profileManagementView.showProfiles(profiles)

            profiles.forEach { profile ->
                profileManagementView.showProfileScore(profile, profile.namebomScore)
            }

            // 테스트: 새 프로필 추가
            if (profiles.isEmpty()) {
                createTestProfile()
                return true  // 프로필을 생성 중
            }

            return false  // 이미 프로필이 있음

        } catch (e: Exception) {
            profileManagementView.showError("프로필 로드 실패: ${e.message}")
            return false
        } finally {
            profileManagementView.showLoading(false)
        }
    }

    suspend fun createProfile() {
        profileInputView.showDynamicNameInput(maxSurname = 2, maxGivenName = 4)
        profileInputView.showYajasiOption()

        if (!profileInputView.validateInput()) {
            return
        }

        try {
            val result = profileModel.createProfile(
                profileName = profileInputView.getProfileName(),
                surname = profileInputView.getSurname(),
                surnameHanja = profileInputView.getSurnameHanja(),
                givenName = profileInputView.getGivenName(),
                givenNameHanja = profileInputView.getGivenNameHanja(),
                birthDateTime = profileInputView.getBirthDateTime(),
                useYajasi = profileInputView.getUseYajasi()
            )

            result.onSuccess { profile ->
                profileInputView.showSuccess("프로필 생성 완료: ${profile.profileName}")
                profileInputView.clearInputs()

                // 프로필 생성 완료 콜백 호출
                onProfileCreated?.invoke(profile)
            }.onFailure { error ->
                profileInputView.showError("프로필 생성 실패: ${error.message}")
            }

        } catch (e: Exception) {
            profileInputView.showError("오류 발생: ${e.message}")
        }
    }

    suspend fun updateProfile(profileId: String) {
        // 구현 예정
    }

    suspend fun deleteProfile(profileId: String) {
        try {
            profileModel.deleteProfile(profileId)
                .onSuccess {
                    logger.d("프로필 삭제 완료")
                }
                .onFailure { error ->
                    profileManagementView.showError("프로필 삭제 실패: ${error.message}")
                }
        } catch (e: Exception) {
            profileManagementView.showError("오류 발생: ${e.message}")
        }
    }

    suspend fun getAllProfiles(): List<Profile> {
        return profileModel.getAllProfiles().getOrDefault(emptyList())
    }

    // 테스트용 프로필 생성
    private suspend fun createTestProfile() {
        logger.d("테스트 프로필 생성 중...")

        (profileInputView as? ProfileInputViewImpl)?.setTestInput(
            profileName = "테스트",
            surname = "김",
            surnameHanja = "金",
            givenName = "민수",
            givenNameHanja = "民秀",
            birthDateTime = LocalDateTime.of(1990, 1, 1, 12, 0),
            useYajasi = false
        )

        createProfile()
    }
}