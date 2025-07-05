// model/domain/usecase/ProfileListSortSearchManager.kt
package com.ssc.namespring.model.domain.usecase

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