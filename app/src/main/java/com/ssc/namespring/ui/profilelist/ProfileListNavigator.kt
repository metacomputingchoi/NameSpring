// ui/profilelist/ProfileListNavigator.kt
package com.ssc.namespring.ui.profilelist

import android.content.Intent
import com.ssc.namespring.ProfileFormActivity
import com.ssc.namespring.ProfileListActivity

class ProfileListNavigator(private val activity: ProfileListActivity) {

    fun navigateToProfileForm(profileId: String? = null) {
        val intent = Intent(activity, ProfileFormActivity::class.java)
        profileId?.let { intent.putExtra("profileId", it) }

        activity.launchProfileForm(intent, profileId != null)
    }
}