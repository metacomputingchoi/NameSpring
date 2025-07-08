// ui/compare/filter/strategies/ElementFilterStrategy.kt
package com.ssc.namespring.ui.compare.filter.strategies

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioGroup
import com.ssc.namespring.R
import com.ssc.namespring.ui.compare.FilterType

class ElementFilterStrategy(
    private val context: Context,
    private val inflater: LayoutInflater
) : FilterStrategy {

    override fun createView(): View {
        return inflater.inflate(R.layout.filter_element, null, false)
    }

    override fun getFilterType(): FilterType = FilterType.ELEMENT

    override fun extractFilterData(view: View): String {
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupElement)
        val selectedId = radioGroup?.checkedRadioButtonId ?: -1

        return when (selectedId) {
            R.id.radioWood -> "木"
            R.id.radioFire -> "火"
            R.id.radioEarth -> "土"
            R.id.radioMetal -> "金"
            R.id.radioWater -> "水"
            else -> ""
        }
    }

    override fun validateInput(view: View): Boolean {
        return extractFilterData(view).isNotEmpty()
    }
}