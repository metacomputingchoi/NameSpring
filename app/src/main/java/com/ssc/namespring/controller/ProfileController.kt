// controller/ProfileController.kt
package com.ssc.namespring.controller

import com.ssc.namespring.model.ProfileModel
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.utils.JsonLoader
import com.ssc.namespring.utils.LoggingHelper
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

    // 프로필 생성 완료 콜백
    var onProfileCreated: ((Profile) -> Unit)? = null

    // 프로필 관리 화면에서 나가기 콜백
    var onProfileManagementClosed: (() -> Unit)? = null

    // 특별 상황 플래그
    private var isCreatingTwinProfile = false
    private var isCreatingAdoptionProfile = false

    suspend fun showProfileManagement(): Boolean {
        profileManagementView.showLoading(true)

        try {
            val profiles = profileModel.getAllProfiles().getOrThrow()
            profileManagementView.showProfiles(profiles)

            profiles.forEach { profile ->
                profileManagementView.showProfileScore(profile, profile.namebomScore)
            }

            // 프로필 추가 버튼 표시
            profileManagementView.showAddProfileDialog()

            // 테스트: 프로필이 없으면 자동으로 추가 화면으로
            if (profiles.isEmpty()) {
                logger.d("프로필이 없어 새 프로필을 생성합니다.")
                createProfile()
                return true
            }

            // 쌍둥이나 형제자매가 있는지 체크
            checkForSiblings(profiles)

            return false

        } catch (e: Exception) {
            profileManagementView.showError("프로필 로드 실패: ${e.message}")
            return false
        } finally {
            profileManagementView.showLoading(false)
        }
    }

    /**
     * 형제자매 체크 및 안내
     */
    private fun checkForSiblings(profiles: List<Profile>) {
        val surnames = profiles.map { it.surname }.distinct()
        if (surnames.size == 1 && profiles.size > 1) {
            // 같은 성씨의 프로필이 여러 개 -> 형제자매일 가능성
            logger.d("")
            logger.d("💡 형제자매를 위한 작명을 하시나요?")

            // 쌍둥이 작명 팁 표시
            JsonLoader.getSpecialConsiderationTips("twins")?.let { tips ->
                logger.d("【${tips.title}】")
                tips.tips?.forEach { tip ->
                    logger.d("• $tip")
                }
            }
        }
    }

    suspend fun createProfile(isTwin: Boolean = false, isAdoption: Boolean = false) {
        isCreatingTwinProfile = isTwin
        isCreatingAdoptionProfile = isAdoption

        logger.d("")
        logger.d("=== 새 프로필 만들기 ===")

        // 특별 상황 안내
        when {
            isTwin -> showTwinProfileGuidance()
            isAdoption -> showAdoptionProfileGuidance()
        }

        profileInputView.showDynamicNameInput(maxSurname = 2, maxGivenName = 4)
        profileInputView.showYajasiOption()

        // 테스트: 자동 입력
        (profileInputView as? ProfileInputViewImpl)?.setTestInput(
            profileName = when {
                isTwin -> "첫째"
                isAdoption -> "입양아"
                else -> "테스트"
            },
            surname = "김",
            surnameHanja = "金",
            givenName = "민수",
            givenNameHanja = "民秀",
            birthDateTime = LocalDateTime.of(1990, 1, 1, 12, 0),
            useYajasi = false
        )

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

                // 프로필 생성 축하 로깅
                LoggingHelper.logProfileCreation(profile)

                // 프로필 생성 완료 콜백 호출
                onProfileCreated?.invoke(profile)
            }.onFailure { error ->
                profileInputView.showError("프로필 생성 실패: ${error.message}")

                // 에러 가이드
                JsonLoader.getErrorMessage("input_errors", "profile_error")?.let {
                    logger.d("💡 $it")
                }
            }

        } catch (e: Exception) {
            profileInputView.showError("오류 발생: ${e.message}")
        }
    }

    /**
     * 쌍둥이 프로필 생성 안내
     */
    private fun showTwinProfileGuidance() {
        logger.d("")
        logger.d("👶👶 쌍둥이 프로필 생성")
        JsonLoader.getSpecialConsiderationTips("twins")?.let { tips ->
            logger.d("【${tips.title}】")
            tips.tips?.forEach { tip ->
                logger.d("• $tip")
            }
        }
    }

    /**
     * 입양아 프로필 생성 안내
     */
    private fun showAdoptionProfileGuidance() {
        logger.d("")
        logger.d("💝 입양아 프로필 생성")
        JsonLoader.getSpecialConsiderationTips("adoption")?.let { tips ->
            logger.d("【${tips.title}】")
            tips.tips?.forEach { tip ->
                logger.d("• $tip")
            }
        }
    }

    suspend fun updateProfile(profileId: String) {
        val profile = profileModel.getProfile(profileId).getOrNull()
        if (profile == null) {
            profileManagementView.showError("프로필을 찾을 수 없습니다")
            return
        }

        profileManagementView.showEditProfileDialog(profile)
        // 실제 구현 시 수정 폼 표시 및 처리
    }

    suspend fun deleteProfile(profileId: String) {
        try {
            val profile = profileModel.getProfile(profileId).getOrNull()
            if (profile != null) {
                profileManagementView.showDeleteConfirmation(profile)

                // 테스트: 자동 확인
                logger.d("프로필 '${profile.profileName}' 삭제 확인")

                profileModel.deleteProfile(profileId)
                    .onSuccess {
                        logger.d("프로필 삭제 완료")
                        showProfileManagement()
                    }
                    .onFailure { error ->
                        profileManagementView.showError("프로필 삭제 실패: ${error.message}")
                    }
            }
        } catch (e: Exception) {
            profileManagementView.showError("오류 발생: ${e.message}")
        }
    }

    suspend fun getAllProfiles(): List<Profile> {
        return profileModel.getAllProfiles().getOrDefault(emptyList())
    }

    suspend fun selectProfile(profile: Profile) {
        logger.d("프로필 선택: ${profile.profileName}")
        onProfileCreated?.invoke(profile)
    }
}