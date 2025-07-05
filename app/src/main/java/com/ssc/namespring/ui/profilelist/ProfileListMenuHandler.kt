// ui/profilelist/ProfileListMenuHandler.kt
package com.ssc.namespring.ui.profilelist

import android.content.Context
import android.view.MenuItem
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.usecase.ProfileListManager

class ProfileListMenuHandler(
    private val listManager: ProfileListManager
) {
    fun handleMenuItemSelected(context: Context, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_mode -> {
                listManager.toggleSelectionMode()
                true
            }
            R.id.action_load_all -> {
                listManager.loadAllProfiles(context)
                true
            }
            else -> false
        }
    }
}