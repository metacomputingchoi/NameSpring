// model/domain/usecase/ProfileListLoadingManager.kt
package com.ssc.namespring.model.domain.usecase

import com.ssc.namespring.model.domain.entity.Profile

class ProfileListLoadingManager {
    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()

    var currentPage = 1
        private set
    val pageSize = 20
    var isLoadingMore = false
        private set
    var hasMoreData = true
        private set
    var allProfiles = listOf<Profile>()
        private set

    fun resetPagination() {
        currentPage = 1
        isLoadingMore = false
        hasMoreData = true
    }

    fun incrementPage() {
        currentPage++
    }

    fun loadProfiles(
        currentQuery: String,
        currentSortType: ProfileManager.SortType,
        currentProfiles: List<Profile>
    ): Pair<List<Profile>, Boolean> {
        allProfiles = if (currentQuery.isEmpty()) {
            profileManager.getSortedProfiles(currentSortType)
        } else {
            profileManager.searchProfiles(currentQuery).let { searchResults ->
                when (currentSortType) {
                    ProfileManager.SortType.NAME_ASC -> searchResults.sortedBy { it.profileName }
                    ProfileManager.SortType.NAME_DESC -> searchResults.sortedByDescending { it.profileName }
                    ProfileManager.SortType.SCORE_DESC -> searchResults.sortedByDescending { it.nameBomScore }
                    ProfileManager.SortType.SCORE_ASC -> searchResults.sortedBy { it.nameBomScore }
                    ProfileManager.SortType.DATE_DESC -> searchResults.sortedByDescending { it.createdAt }
                    ProfileManager.SortType.DATE_ASC -> searchResults.sortedBy { it.createdAt }
                }
            }
        }

        val startIndex = (currentPage - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, allProfiles.size)

        val newProfiles = if (currentPage == 1) {
            allProfiles.subList(0, minOf(pageSize, allProfiles.size))
        } else {
            currentProfiles + allProfiles.subList(startIndex, endIndex)
        }

        hasMoreData = endIndex < allProfiles.size
        isLoadingMore = false

        return Pair(newProfiles, hasMoreData)
    }

    fun canLoadMore(): Boolean = !isLoadingMore && hasMoreData

    fun startLoadingMore() {
        isLoadingMore = true
    }

    fun loadAllAtOnce(
        currentQuery: String,
        currentSortType: ProfileManager.SortType
    ): List<Profile> {
        hasMoreData = false
        return if (currentQuery.isEmpty()) {
            profileManager.getSortedProfiles(currentSortType)
        } else {
            profileManager.searchProfiles(currentQuery)
        }
    }
}