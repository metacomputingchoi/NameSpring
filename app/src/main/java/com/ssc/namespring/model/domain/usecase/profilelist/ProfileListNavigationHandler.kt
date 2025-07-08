// model/domain/usecase/profilelist/ProfileListNavigationHandler.kt
package com.ssc.namespring.model.domain.usecase.profilelist

import android.content.Context
import android.content.Intent
import com.ssc.namespring.MainActivity
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.usecase.ProfileManager
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider

class ProfileListNavigationHandler(
    private val selectionHandler: ProfileListSelectionHandler
) {
    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()

    fun handleProfileClick(context: Context, profile: Profile) {
        if (selectionHandler.isInSelectionMode()) {
            selectionHandler.toggleSelection(profile.id)
        } else {
            profileManager.switchProfile(profile.id)
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    fun handleProfileLongClick(profile: Profile): Boolean {
        if (!selectionHandler.isInSelectionMode()) {
            selectionHandler.enterSelectionMode()
            selectionHandler.toggleSelection(profile.id)
        }
        return true
    }
}
