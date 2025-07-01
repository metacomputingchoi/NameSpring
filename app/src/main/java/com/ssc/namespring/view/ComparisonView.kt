// view/ComparisonView.kt
package com.ssc.namespring.view

import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.data.ComparisonReport
import com.ssc.namespring.model.data.Profile

interface ComparisonView {
    fun showNameSelectors(count: Int)
    fun showProfileSelector(profiles: List<Profile>)
    fun showComparisonTable(report: ComparisonReport)
    fun showRankings(rankings: List<com.ssc.namespring.model.data.RankingResult>)
    fun showWinner(winner: GeneratedName)
    fun enableCompareButton(enabled: Boolean)
    fun showError(message: String)
    fun showLoading(isLoading: Boolean)
}