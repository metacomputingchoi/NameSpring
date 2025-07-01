// view/impl/ProfileManagementViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.ProfileManagementView

class ProfileManagementViewImpl(private val activity: Activity) : ProfileManagementView {

    private val logger = AndroidLogger("ProfileManagementView")

    override fun showProfiles(profiles: List<Profile>) {
        logger.d("=== 프로필 목록 ===")
        if (profiles.isEmpty()) {
            logger.d("등록된 프로필이 없습니다")
        } else {
            profiles.forEachIndexed { index, profile ->
                logger.d("${index + 1}. ${profile.profileName} - ${profile.getFullName()} (${profile.namebomScore}점)")
            }
        }
    }

    override fun showAddProfileDialog() {
        logger.d("새 프로필 추가 다이얼로그 표시")
    }

    override fun showEditProfileDialog(profile: Profile) {
        logger.d("프로필 수정 다이얼로그 표시: ${profile.profileName}")
    }

    override fun showDeleteConfirmation(profile: Profile) {
        logger.d("프로필 삭제 확인: ${profile.profileName}을(를) 삭제하시겠습니까?")
    }

    override fun showProfileScore(profile: Profile, score: Int) {
        val sprout = when (score) {
            in 80..100 -> "🌸"
            in 60..79 -> "🌳"
            in 40..59 -> "🌿"
            in 20..39 -> "🌱"
            else -> "🌰"
        }
        logger.d("$sprout ${profile.profileName}: ${score}점")
    }

    override fun showError(message: String) {
        logger.e("오류: $message")
    }

    override fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            logger.d("로딩 중...")
        }
    }
}