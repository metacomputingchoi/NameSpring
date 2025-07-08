// ui/compare/filter/TextInputFilterHandler.kt
package com.ssc.namespring.ui.compare.filter

import android.content.Context
import android.view.View
import android.widget.*
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.filter.FilterConfig
import com.ssc.namespring.ui.compare.FilterType  // 변경됨

class TextInputFilterHandler(
    private val context: Context,
    private val filterType: FilterType,
    private val checkBoxId: Int,
    private val containerId: Int,
    private val editTextId: Int,
    private val addButtonId: Int,
    private val chipGroupId: Int,
    private val duplicateMessage: String
) : IFilterHandler {
    // 나머지 코드는 동일
    private var checkBox: CheckBox? = null
    private var container: LinearLayout? = null
    private var editText: TextInputEditText? = null
    private var addButton: ImageButton? = null
    private var chipGroup: ChipGroup? = null
    private val items = mutableListOf<String>()

    override fun setupView(view: View) {
        checkBox = view.findViewById(checkBoxId)
        container = view.findViewById(containerId)
        editText = view.findViewById(editTextId)
        addButton = view.findViewById(addButtonId)
        chipGroup = view.findViewById(chipGroupId)

        setupCheckBox()
        setupAddButton()
    }

    private fun setupCheckBox() {
        checkBox?.setOnCheckedChangeListener { _, isChecked ->
            container?.alpha = if (isChecked) 1.0f else 0.5f
            editText?.isEnabled = isChecked
            addButton?.isEnabled = isChecked

            if (!isChecked) {
                reset()
            }
        }
    }

    private fun setupAddButton() {
        addButton?.setOnClickListener {
            if (isEnabled()) {
                val text = editText?.text?.toString()?.trim()
                if (!text.isNullOrBlank()) {
                    if (!items.contains(text)) {
                        items.add(text)
                        addChip(text)
                        editText?.text?.clear()
                    } else {
                        Toast.makeText(context, duplicateMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun addChip(text: String) {
        val chip = Chip(context).apply {
            this.text = text
            isCloseIconVisible = true
            setChipBackgroundColorResource(R.color.chip_background)

            setOnCloseIconClickListener {
                items.remove(text)
                chipGroup?.removeView(this)
            }
        }
        chipGroup?.addView(chip)
    }

    override fun collectFilters(): List<FilterConfig> {
        return items.map { FilterConfig(filterType, it) }
    }

    override fun reset() {
        items.clear()
        chipGroup?.removeAllViews()
        editText?.text?.clear()
    }

    override fun isEnabled(): Boolean = checkBox?.isChecked ?: false
}