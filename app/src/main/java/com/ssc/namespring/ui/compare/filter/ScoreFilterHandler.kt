// ui/compare/filter/ScoreFilterHandler.kt
package com.ssc.namespring.ui.compare.filter

import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.slider.RangeSlider
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.filter.FilterConfig
import com.ssc.namespring.model.domain.filter.ScoreRange
import com.ssc.namespring.ui.compare.FilterType  // 변경됨

class ScoreFilterHandler(
    private val minScore: Int,
    private val maxScore: Int
) : IFilterHandler {
    // 나머지 코드는 동일
    private var checkBox: CheckBox? = null
    private var rangeSlider: RangeSlider? = null
    private var tvScoreRange: TextView? = null
    private var container: LinearLayout? = null

    override fun setupView(view: View) {
        checkBox = view.findViewById(R.id.cbScoreRange)
        rangeSlider = view.findViewById(R.id.sliderScore)
        tvScoreRange = view.findViewById(R.id.tvScoreRange)
        container = view.findViewById(R.id.containerScoreRange)

        setupRangeSlider()
        setupCheckBox()
    }

    private fun setupRangeSlider() {
        rangeSlider?.apply {
            valueFrom = minScore.toFloat()
            valueTo = maxScore.toFloat()
            values = listOf(minScore.toFloat(), maxScore.toFloat())

            addOnChangeListener { slider, _, _ ->
                val values = slider.values
                val min = values[0].toInt()
                val max = values[1].toInt()
                tvScoreRange?.text = "$min ~ ${max}점"
            }
        }

        tvScoreRange?.text = "$minScore ~ ${maxScore}점"
    }

    private fun setupCheckBox() {
        checkBox?.setOnCheckedChangeListener { _, isChecked ->
            container?.alpha = if (isChecked) 1.0f else 0.5f
            rangeSlider?.isEnabled = isChecked

            if (!isChecked) {
                reset()
            }
        }
    }

    override fun collectFilters(): List<FilterConfig> {
        if (!isEnabled()) return emptyList()

        val values = rangeSlider?.values ?: return emptyList()
        val min = values[0].toInt()
        val max = values[1].toInt()

        return if (min != minScore || max != maxScore) {
            listOf(FilterConfig(FilterType.SCORE_RANGE, IntRange(min, max)))
        } else emptyList()
    }

    override fun reset() {
        rangeSlider?.values = listOf(minScore.toFloat(), maxScore.toFloat())
        tvScoreRange?.text = "$minScore ~ ${maxScore}점"
    }

    override fun isEnabled(): Boolean = checkBox?.isChecked ?: false
}