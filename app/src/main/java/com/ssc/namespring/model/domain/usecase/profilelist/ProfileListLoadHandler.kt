// model/domain/usecase/profilelist/ProfileListLoadHandler.kt
package com.ssc.namespring.model.domain.usecase.profilelist

import com.ssc.namespring.model.domain.usecase.ProfileListLoadingManager
import com.ssc.namespring.model.domain.usecase.ProfileListSortSearchManager
import com.ssc.namespring.model.domain.entity.Profile

class ProfileListLoadHandler(
    private val loadingManager: ProfileListLoadingManager,
    private val sortSearchManager: ProfileListSortSearchManager,
    private val uiStateManager: ProfileListUiStateManager
) {
    fun loadProfiles() {
        val currentState = uiStateManager.getCurrentState() ?: return
        if (currentState.isLoading) return

        uiStateManager.updateLoadingState(true)

        val (newProfiles, _) = loadingManager.loadProfiles(
            sortSearchManager.currentQuery,
            sortSearchManager.currentProfileManagerSortType,
            currentState.profiles
        )

        uiStateManager.updateProfiles(newProfiles)
    }

    fun loadMoreProfiles() {
        if (loadingManager.canLoadMore()) {
            loadingManager.startLoadingMore()
            loadingManager.incrementPage()
            loadProfiles()
        }
    }

    fun refreshProfiles() {
        loadingManager.resetPagination()
        uiStateManager.updateProfiles(emptyList())
        loadProfiles()
    }

    fun loadAllAtOnce(): List<Profile> {
        uiStateManager.updateLoadingState(true)
        val profiles = loadingManager.loadAllAtOnce(
            sortSearchManager.currentQuery,
            sortSearchManager.currentProfileManagerSortType
        )
        uiStateManager.updateProfiles(profiles)
        return profiles
    }
}
