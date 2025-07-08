// ui/compare/CompareSortManager.kt
package com.ssc.namespring.ui.compare

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.ssc.namespring.R

class CompareSortManager(
    private val context: Context,
    private val chipGroupSort: ChipGroup,
    private val viewModel: CompareViewModel
) {
    fun setupSortChips() {
        val sortOptions = listOf(
            SortType.NAME to "이름순",
            SortType.SCORE to "점수순",
            SortType.BIRTH_DATE to "생년월일순",
            SortType.ADDED_DATE to "추가일순"
        )

        sortOptions.forEach { (sortType, label) ->
            val chip = createSortChip(sortType, label)
            chipGroupSort.addView(chip)
        }
    }

    private fun createSortChip(sortType: SortType, label: String): View {
        val chipLayout = LayoutInflater.from(context)
            .inflate(R.layout.item_sort_chip, chipGroupSort, false)

        val chip = chipLayout.findViewById<Chip>(R.id.chip)
        val btnSort = chipLayout.findViewById<ImageButton>(R.id.btnSort)

        chip.text = label
        chip.isCheckable = true
        chip.tag = sortType
        chip.isChecked = false
        btnSort.visibility = View.GONE

        chip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.toggleSort(sortType)
                btnSort.visibility = View.VISIBLE
                updateSortButton(btnSort, sortType)
            } else {
                viewModel.removeSort(sortType)
                btnSort.visibility = View.GONE
            }
        }

        btnSort.setOnClickListener {
            viewModel.toggleSort(sortType)
            updateSortButton(btnSort, sortType)
        }

        return chipLayout
    }

    private fun updateSortButton(button: ImageButton, sortType: SortType) {
        val sorts = viewModel.activeSorts.value ?: return
        val sortInfo = sorts.find { it.type == sortType } ?: return

        button.setImageResource(
            if (sortInfo.ascending) R.drawable.ic_arrow_upward
            else R.drawable.ic_arrow_downward
        )
    }

    fun updateSortChips(sorts: List<SortInfo>) {
        for (i in 0 until chipGroupSort.childCount) {
            val chipLayout = chipGroupSort.getChildAt(i)
            val chip = chipLayout.findViewById<Chip>(R.id.chip)
            val btnSort = chipLayout.findViewById<ImageButton>(R.id.btnSort)

            val sortType = chip.tag as? SortType ?: continue
            val sortInfo = sorts.find { it.type == sortType }

            if (sortInfo != null) {
                chip.isChecked = true
                btnSort.visibility = View.VISIBLE
                btnSort.setImageResource(
                    if (sortInfo.ascending) R.drawable.ic_arrow_upward
                    else R.drawable.ic_arrow_downward
                )
            } else {
                chip.isChecked = false
                btnSort.visibility = View.GONE
            }
        }
    }
}