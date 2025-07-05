// model/business/ProfileListSortSearchManager.kt
package com.ssc.namespring.model.business

import com.ssc.namespring.model.repository.ProfileManager

class ProfileListSortSearchManager {
    var currentSortType = ProfileManager.SortType.DATE_DESC
        private set
    var currentQuery = ""
        private set

    fun updateSortType(sortType: ProfileManager.SortType) {
        currentSortType = sortType
    }

    fun updateSearchQuery(query: String) {
        currentQuery = query
    }
}