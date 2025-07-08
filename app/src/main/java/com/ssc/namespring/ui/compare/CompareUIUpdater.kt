// ui/compare/CompareUIUpdater.kt
package com.ssc.namespring.ui.compare

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteName

class CompareUIUpdater(
    private val activity: Activity,
    private val chipGroupFilters: ChipGroup,
    private val fabCompare: ExtendedFloatingActionButton,
    private val containerSource: LinearLayout,
    private val containerTarget: LinearLayout,
    private val viewModel: CompareViewModel
) {
    fun updateEmptyView(emptyView: TextView, isEmpty: Boolean, type: String) {
        if (isEmpty) {
            emptyView.visibility = View.VISIBLE
            emptyView.text = when (type) {
                "source" -> when {
                    viewModel.showDeleted.value == true -> "삭제된 즐겨찾기가 없습니다"
                    viewModel.hasActiveFilters() -> "필터 조건에 맞는 이름이 없습니다"
                    else -> "즐겨찾기된 이름이 없습니다"
                }
                "target" -> "비교할 이름을 선택하세요\n(오른쪽으로 스와이프)"
                else -> ""
            }
        } else {
            emptyView.visibility = View.GONE
        }
    }

    fun updateCompareButton(count: Int) {
        when {
            count == 0 -> {
                fabCompare.text = "비교할 이름 선택"
                fabCompare.isEnabled = false
                fabCompare.hide()
            }
            count == 1 -> {
                fabCompare.text = "1개 더 선택 필요"
                fabCompare.isEnabled = false
                fabCompare.show()
            }
            else -> {
                fabCompare.text = "${count}개 이름 비교하기"
                fabCompare.isEnabled = true
                fabCompare.show()
            }
        }
    }

    fun updateComparisonMode(hasComparison: Boolean) {
        if (hasComparison) {
            containerSource.layoutParams = (containerSource.layoutParams as LinearLayout.LayoutParams).apply {
                weight = 1f
            }
            containerTarget.visibility = View.VISIBLE
        } else {
            containerSource.layoutParams = (containerSource.layoutParams as LinearLayout.LayoutParams).apply {
                weight = 0f
                width = LinearLayout.LayoutParams.MATCH_PARENT
            }
            containerTarget.visibility = View.GONE
        }
    }

    fun updateFilterChips(filters: List<FilterInfo>) {
        chipGroupFilters.removeAllViews()

        filters.forEach { filterInfo ->
            val chip = Chip(activity).apply {
                text = filterInfo.displayName
                isCloseIconVisible = true
                setChipBackgroundColorResource(R.color.chip_background)
                setOnCloseIconClickListener {
                    viewModel.removeFilter(filterInfo.id)
                }
            }
            chipGroupFilters.addView(chip)
        }
    }

    fun updateSelectionInfo(selectedItems: List<FavoriteName>) {
        val selectionInfo = activity.findViewById<TextView>(R.id.tvSelectionInfo)
        if (selectedItems.isNotEmpty()) {
            selectionInfo?.apply {
                visibility = View.VISIBLE
                text = "선택된 이름: ${selectedItems.joinToString(", ") { it.fullNameKorean }}"
            }
        } else {
            selectionInfo?.visibility = View.GONE
        }
    }
}