// ui/profilelist/ProfileListComponents.kt
package com.ssc.namespring.ui.profilelist

import com.ssc.namespring.ProfileListActivity

class ProfileListComponents(private val activity: ProfileListActivity) {
    val viewHolder = ProfileListViewHolder(activity)
    val eventHandler = ProfileListEventHandler(activity, viewHolder)
    val uiUpdater = ProfileListUiUpdater(activity, viewHolder)

    fun initializeAll() {
        viewHolder.initViews()
        eventHandler.setupAllEvents()
    }
}