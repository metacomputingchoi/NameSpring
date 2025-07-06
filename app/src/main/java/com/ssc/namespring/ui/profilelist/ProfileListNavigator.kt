// ui/profilelist/ProfileListNavigator.kt
package com.ssc.namespring.ui.profilelist

import com.ssc.namespring.ProfileFormActivity
import com.ssc.namespring.ProfileListActivity
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.entity.ProfileFormMode

class ProfileListNavigator(private val activity: ProfileListActivity) {

    fun navigateToProfileForm(profileId: String? = null) {
        val config = if (profileId != null) {
            ProfileFormConfig(
                mode = ProfileFormMode.EDIT,
                profileId = profileId
            )
        } else {
            ProfileFormConfig(
                mode = ProfileFormMode.CREATE
            )
        }

        val intent = ProfileFormActivity.newIntent(activity, config)
        activity.launchProfileForm(intent, profileId != null)
    }
}