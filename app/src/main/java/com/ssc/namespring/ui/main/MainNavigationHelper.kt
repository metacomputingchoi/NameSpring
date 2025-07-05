// ui/main/MainNavigationHelper.kt
package com.ssc.namespring.ui.main

import android.app.Activity
import android.content.Intent
import com.ssc.namespring.ProfileListActivity

class MainNavigationHelper(private val activity: Activity) {

    fun navigateToProfileList() {
        activity.startActivity(Intent(activity, ProfileListActivity::class.java))
        activity.finish()
    }
}