// ui/profileform/ProfileSaveHandler.kt
package com.ssc.namespring.ui.profileform

import android.util.Log
import android.widget.Toast
import com.ssc.namespring.ProfileFormActivity
import com.ssc.namespring.model.domain.entity.ProfileFormMode
import com.ssc.namespring.model.domain.service.profile.ProfileFormService
import com.ssc.namespring.model.domain.usecase.ProfileFormManager

class ProfileSaveHandler(
    private val activity: ProfileFormActivity,
    private val profileFormService: ProfileFormService
) {
    companion object {
        private const val TAG = "ProfileSaveHandler"
    }

    fun saveProfile(
        formManager: ProfileFormManager,
        profileName: String,
        profileId: String?,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        Log.d(TAG, "Saving profile: name=$profileName, profileId=$profileId")

        profileFormService.saveProfile(
            activity,
            formManager,
            profileName,
            profileId
        ) { success ->
            if (success) {
                Log.d(TAG, "Profile saved successfully")
                onSuccess()
            } else {
                Log.e(TAG, "Failed to save profile")
                onFailure()
            }
        }
    }
}