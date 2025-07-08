// ui/compare/filter/strategies/ScoreRangeFilterStrategy.kt
package com.ssc.namespring.ui.compare.filter.strategies

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.ssc.namespring.R
import com.ssc.namespring.ui.compare.FilterType

class ScoreRangeFilterStrategy(
    private val context: Context,
    private val inflater: LayoutInflater
) : FilterStrategy {

    override fun createView(): View {
        return inflater.inflate(R.layout.filter_score_range, null, false)
    }

    override fun getFilterType(): FilterType = FilterType.SCORE_RANGE

    override fun extractFilterData(view: View): Pair<Int, Int> {
        val minScore = view.findViewById<EditText>(R.id.etMinScore)
            ?.text?.toString()?.toIntOrNull() ?: 0
        val maxScore = view.findViewById<EditText>(R.id.etMaxScore)
            ?.text?.toString()?.toIntOrNull() ?: 100
        return Pair(minScore, maxScore)
    }

    override fun validateInput(view: View): Boolean = true
}