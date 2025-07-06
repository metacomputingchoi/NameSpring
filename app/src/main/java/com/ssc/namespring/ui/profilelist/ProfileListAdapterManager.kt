// ui/profilelist/ProfileListAdapterManager.kt
package com.ssc.namespring.ui.profilelist

import com.ssc.namespring.ProfileListActivity
import com.ssc.namespring.model.domain.service.profile.ProfileEvaluationService
import com.ssc.namespring.model.presentation.adapter.ProfileAdapter
import com.ssc.namespring.model.domain.usecase.ProfileListManager
import com.ssc.namespring.model.domain.usecase.ProfileManager
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namingengine.NamingEngine

class ProfileListAdapterManager(
    private val activity: ProfileListActivity,
    private val listManager: ProfileListManager,
    private val navigator: ProfileListNavigator,
    private val components: ProfileListComponents
) {
    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()

    fun createAdapter(): ProfileAdapter {
        return ProfileAdapter(
            onItemClick = { profile ->
                listManager.onProfileClick(activity, profile)
            },
            onItemLongClick = { profile ->
                listManager.onProfileLongClick(profile)
            },
            // 편집 클릭시 재평가 제거
            onEditClick = { profile ->
                // 재평가 없이 바로 편집 화면으로
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