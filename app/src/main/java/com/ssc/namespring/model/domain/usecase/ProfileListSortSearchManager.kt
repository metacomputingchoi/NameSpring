// model/domain/usecase/ProfileListSortSearchManager.kt
package com.ssc.namespring.model.domain.usecase

class ProfileListSortSearchManager {
    var currentProfileManagerSortType = ProfileManager.ProfileManagerSortType.DATE_DESC
        private set
    var currentQuery = ""
        private set

    fun updateSortType(profileManagerSortType: ProfileManager.ProfileManagerSortType) {
        currentProfileManagerSortType = profileManagerSortType
    }

    fun updateSearchQuery(query: String) {
        currentQuery = query
    }
}