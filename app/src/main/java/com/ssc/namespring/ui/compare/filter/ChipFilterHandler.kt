// ui/compare/filter/ChipFilterHandler.kt
package com.ssc.namespring.ui.compare.filter

import android.content.Context
import android.view.View
import android.widget.CheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.filter.FilterConfig
import com.ssc.namespring.ui.compare.FilterType  // 변경됨

class ChipFilterHandler(
    private val context: Context,
    private val filterType: FilterType,
    private val checkBoxId: Int,
    private val chipGroupId: Int,
    private val items: List<Pair<String, String>>
) : IFilterHandler {
    // 나머지 코드는 동일
    private var checkBox: CheckBox? = null
    private var chipGroup: ChipGroup? = null
    private val selectedItems = mutableSetOf<String>()

    override fun setupView(view: View) {
        checkBox = view.findViewById(checkBoxId)
        chipGroup = view.findViewById(chipGroupId)

        setupCheckBox()
        setupChips()
    }

    private fun setupCheckBox() {
        checkBox?.setOnCheckedChangeListener { _, isChecked ->
            chipGroup?.alpha = if (isChecked) 1.0f else 0.5f
            updateChipsEnabled(isChecked)

            if (!isChecked) {
                reset()
            }
        }
    }

    private fun setupChips() {
        chipGroup?.removeAllViews()

        items.forEach { (text, value) ->
            val chip = createChip(text, value)
            chipGroup?.addView(chip)
        }
    }

    private fun createChip(text: String, value: String): Chip {
        return Chip(context).apply {
            this.text = text
            isCheckable = true
            isEnabled = false

            val states = arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            )
            val colors = intArrayOf(
                context.getColor(R.color.primary_blue),
                context.getColor(R.color.chip_background)
            )
            chipBackgroundColor = android.content.res.ColorStateList(states, colors)

            setTextAppearance(R.style.ChipTextAppearance)

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedItems.add(value)
                else selectedItems.remove(value)
            }
        }
    }

    private fun updateChipsEnabled(enabled: Boolean) {
        chipGroup?.let { group ->
            for (i in 0 until group.childCount) {
                group.getChildAt(i).isEnabled = enabled
            }
        }
    }

    override fun collectFilters(): List<FilterConfig> {
        return selectedItems.map { FilterConfig(filterType, it) }
    }

    override fun reset() {
        selectedItems.clear()
        chipGroup?.clearCheck()
    }

    override fun isEnabled(): Boolean = checkBox?.isChecked ?: false
}