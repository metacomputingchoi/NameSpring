// ui/profilelist/ProfileListLauncherManager.kt
package com.ssc.namespring.ui.profilelist

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.ssc.namespring.model.domain.usecase.ProfileListManager

class ProfileListLauncherManager(
    activity: ComponentActivity,
    private val listManager: ProfileListManager
) {
    private val createProfileLauncher: ActivityResultLauncher<Intent>
    private val editProfileLauncher: ActivityResultLauncher<Intent>

    init {
        val resultCallback = { _: androidx.activity.result.ActivityResult ->
            listManager.refreshProfiles()
        }

        createProfileLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            resultCallback
        )

        editProfileLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            resultCallback
        )
    }

    fun launchProfileForm(intent: Intent, isEdit: Boolean) {
        if (isEdit) {
            editProfileLauncher.launch(intent)
        } else {
            createProfileLauncher.launch(intent)
        }
    }
}