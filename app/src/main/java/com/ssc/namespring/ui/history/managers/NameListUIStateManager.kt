// ui/history/managers/NameListUIStateManager.kt
package com.ssc.namespring.ui.history.managers

import android.util.Log
import com.ssc.namespring.ui.history.managers.NameListSortManager.NameListSortOrder
import com.ssc.namespring.utils.search.NameSearchHelper
import com.ssc.namingengine.data.GeneratedName
import java.text.Collator
import java.util.*

class NameListUIStateManager {
    companion object {
        private const val TAG = "NameListUIStateManager"
    }

    private val koreanCollator = Collator.getInstance(Locale.KOREAN)

    private var allNames: List<GeneratedName> = emptyList()
    var currentSearchQuery = ""
        private set
    private var currentNameListSortOrder = NameListSortOrder.SCORE_DESC

    fun updateData(names: List<GeneratedName>) {
        allNames = names
    }

    fun updateSearchQuery(query: String) {
        currentSearchQuery = query
    }

    fun updateSortOrder(order: NameListSortOrder) {
        currentNameListSortOrder = order
    }

    fun getFilteredAndSortedNames(searchHelper: NameSearchHelper): List<GeneratedName> {
        Log.d(TAG, "Filtering and sorting. Query: '$currentSearchQuery', Sort: $currentNameListSortOrder")

        // 필터링
        val filtered = if (currentSearchQuery.isEmpty()) {
            allNames
        } else {
            allNames.filter { name ->
                searchHelper.matches(name, currentSearchQuery)
            }
        }

        Log.d(TAG, "Filtered ${filtered.size} names from ${allNames.size}")

        // 정렬
        return when (currentNameListSortOrder) {
            NameListSortOrder.SCORE_DESC -> filtered.sortedByDescending {
                it.analysisInfo?.totalScore ?: 0 
            }
            NameListSortOrder.SCORE_ASC -> filtered.sortedBy {
                it.analysisInfo?.totalScore ?: 0 
            }
            NameListSortOrder.NAME_ASC -> filtered.sortedWith { a, b ->
                koreanCollator.compare(a.combinedPronounciation, b.combinedPronounciation)
            }
            NameListSortOrder.NAME_DESC -> filtered.sortedWith { a, b ->
                koreanCollator.compare(b.combinedPronounciation, a.combinedPronounciation)
            }
        }
    }
}