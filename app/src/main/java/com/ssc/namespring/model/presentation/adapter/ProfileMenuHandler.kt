// model/presentation/adapter/ProfileMenuHandler.kt
package com.ssc.namespring.model.presentation.adapter

import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.snackbar.Snackbar
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.usecase.ProfileManager
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider

class ProfileMenuHandler(
    private val onEditClick: (Profile) -> Unit,
    private val onDeleteClick: (Profile) -> Unit,
    private val onDuplicateClick: (Profile) -> Unit
) {
    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()

    fun showMenu(view: View, profile: Profile) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.profile_item_menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    onEditClick(profile)
                    true
                }
                R.id.action_delete -> {
                    onDeleteClick(profile)
                    true
                }
                R.id.action_duplicate -> {
                    duplicateProfile(view, profile)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun duplicateProfile(view: View, profile: Profile) {
        val newProfile = profile.copy(
            id = System.currentTimeMillis().toString(),
            profileName = "${profile.profileName} (복사본)",
            createdAt = System.currentTimeMillis()
        )

        if (profileManager.addProfile(newProfile)) {
            Snackbar.make(
                view,
                "프로필이 복제되었습니다",
                Snackbar.LENGTH_SHORT
            ).show()
            onDuplicateClick(profile)
        } else {
            Snackbar.make(
                view,
                "프로필 복제에 실패했습니다",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}