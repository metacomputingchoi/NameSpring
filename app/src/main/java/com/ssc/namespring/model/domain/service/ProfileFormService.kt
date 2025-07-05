// model/domain/service/ProfileFormService.kt
package com.ssc.namespring.model.domain.service

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.ssc.namespring.model.domain.usecase.ProfileFormManager
import com.ssc.namespring.model.domain.usecase.ProfileManager

class ProfileFormService {

    fun saveProfile(
        context: Context,
        formManager: ProfileFormManager,
        profileName: String,
        profileId: String?,
        callback: (Boolean) -> Unit
    ) {
        try {
            val profile = formManager.createProfile(profileName)

            val success = if (!profileId.isNullOrEmpty()) {
                ProfileManager.updateProfile(profile)
            } else {
                ProfileManager.addProfile(profile)
            }

            if (!success) {
                showDuplicateProfileDialog(context)
            }

            callback(success)
        } catch (e: Exception) {
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