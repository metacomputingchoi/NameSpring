// model/domain/usecase/ProfileListDeleteManager.kt
package com.ssc.namespring.model.domain.usecase

import android.R
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.ssc.namespring.model.domain.entity.Profile

class ProfileListDeleteManager {
    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()

    fun showDeleteConfirmDialog(
        context: Context,
        profileIds: List<String>,
        profiles: List<Profile>,
        onDeleteConfirmed: () -> Unit
    ) {
        Log.d("ProfileListManager", "showDeleteConfirmDialog called with ${profileIds.size} profiles")

        val message = if (profileIds.size == 1) {
            val profile = profiles.find { it.id == profileIds[0] }
            "'${profile?.profileName}' 프로필을 삭제하시겠습니까?"
        } else {
            "${profileIds.size}개의 프로필을 삭제하시겠습니까?"
        }

        try {
            AlertDialog.Builder(context)
                .setTitle("프로필 삭제")
                .setMessage(message)
                .setPositiveButton("삭제") { _, _ ->
                    Log.d("ProfileListManager", "Delete confirmed")
                    profileManager.deleteProfiles(profileIds)
                    Log.d("ProfileListManager", "ProfileManager.deleteProfiles called")

                    onDeleteConfirmed()

                    val snackbarMessage = if (profileIds.size == 1) {
                        "프로필이 삭제되었습니다"
                    } else {
                        "${profileIds.size}개의 프로필이 삭제되었습니다"
                    }

                    Snackbar.make(
                        (context as AppCompatActivity).findViewById(R.id.content),
                        snackbarMessage,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("취소", null)
                .show()

            Log.d("ProfileListManager", "Dialog shown")
        } catch (e: Exception) {
            Log.e("ProfileListManager", "Error showing dialog", e)
        }
    }
}