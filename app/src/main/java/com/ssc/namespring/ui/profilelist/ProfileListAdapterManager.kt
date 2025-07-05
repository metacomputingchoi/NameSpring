// ui/profilelist/ProfileListAdapterManager.kt
package com.ssc.namespring.ui.profilelist

import com.ssc.namespring.ProfileListActivity
import com.ssc.namespring.model.adapter.ProfileAdapter
import com.ssc.namespring.model.business.ProfileListManager
import com.ssc.namespring.profile.ProfileListComponents
import com.ssc.namespring.profile.ProfileListNavigator

class ProfileListAdapterManager(
    private val activity: ProfileListActivity,
    private val listManager: ProfileListManager,
    private val navigator: ProfileListNavigator,
    private val components: ProfileListComponents
) {
    fun createAdapter(): ProfileAdapter {
        return ProfileAdapter(
            onItemClick = { profile ->
                listManager.onProfileClick(activity, profile)
            },
            onItemLongClick = { profile ->
                listManager.onProfileLongClick(profile)
            },
            onEditClick = { profile ->
                navigator.navigateToProfileForm(profile.id)
            },
            onDeleteClick = { profile ->
                listManager.deleteProfile(activity, profile)
            },
            onDuplicateClick = { _ ->
                listManager.refreshProfiles()
            }
        )
    }
}