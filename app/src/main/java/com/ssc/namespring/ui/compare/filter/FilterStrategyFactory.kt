// ui/compare/filter/FilterStrategyFactory.kt
package com.ssc.namespring.ui.compare.filter

import android.content.Context
import android.view.LayoutInflater
import com.ssc.namespring.ui.compare.filter.strategies.*

class FilterStrategyFactory(
    private val context: Context,
    private val inflater: LayoutInflater
) {
    fun createStrategy(position: Int): FilterStrategy {
        return when (position) {
            0 -> ScoreRangeFilterStrategy(context, inflater)
            1 -> DateRangeFilterStrategy(context, inflater)
            2 -> SurnameFilterStrategy(context, inflater)
            3 -> HanjaFilterStrategy(context, inflater)
            4 -> ElementFilterStrategy(context, inflater)
            else -> throw IllegalArgumentException("Unknown filter position: $position")
        }
    }

    fun getFilterTypes(): Array<String> {
        return arrayOf(
            "점수 범위",
            "생년월일 범위",
            "성씨",
            "한자 포함",
            "오행"
        )
    }
}