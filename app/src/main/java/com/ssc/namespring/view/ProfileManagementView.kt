// view/ProfileManagementView.kt
package com.ssc.namespring.view

import com.ssc.namespring.model.data.Profile

interface ProfileManagementView {
    fun showProfiles(profiles: List<Profile>)
    fun showAddProfileDialog()
    fun showEditProfileDialog(profile: Profile)
    fun showDeleteConfirmation(profile: Profile)
    fun showProfileScore(profile: Profile, score: Int)
    fun showError(message: String)
    fun showLoading(isLoading: Boolean)
}