// ui/history/managers/NameListUIStateManager.kt
package com.ssc.namespring.ui.history.managers

import android.util.Log
import com.ssc.namespring.ui.history.managers.NameListSortManager.SortOrder
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
    private var currentSortOrder = SortOrder.SCORE_DESC

    fun updateData(names: List<GeneratedName>) {
        allNames = names
    }

    fun updateSearchQuery(query: String) {
        currentSearchQuery = query
    }

    fun updateSortOrder(order: SortOrder) {
        currentSortOrder = order
    }

    fun getFilteredAndSortedNames(searchHelper: NameSearchHelper): List<GeneratedName> {
        Log.d(TAG, "Filtering and sorting. Query: '$currentSearchQuery', Sort: $currentSortOrder")

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
        return when (currentSortOrder) {
            SortOrder.SCORE_DESC -> filtered.sortedByDescending { 
                it.analysisInfo?.totalScore ?: 0 
            }
            SortOrder.SCORE_ASC -> filtered.sortedBy { 
                it.analysisInfo?.totalScore ?: 0 
            }
            SortOrder.NAME_ASC -> filtered.sortedWith { a, b ->
                koreanCollator.compare(a.combinedPronounciation, b.combinedPronounciation)
            }
            SortOrder.NAME_DESC -> filtered.sortedWith { a, b ->
                koreanCollator.compare(b.combinedPronounciation, a.combinedPronounciation)
            }
        }
    }
}