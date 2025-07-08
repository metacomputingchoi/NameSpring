// utils/common/SortingUtils.kt
package com.ssc.namespring.utils.common

import android.view.LayoutInflater
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.usecase.ProfileManager

object SortingUtils {

    fun setupSortChips(
        chipGroup: ChipGroup,
        inflater: LayoutInflater,
        onSortChanged: (ProfileManager.ProfileManagerSortType) -> Unit
    ) {
        val sortOptions = listOf(
            "최신순" to ProfileManager.ProfileManagerSortType.DATE_DESC,
            "오래된순" to ProfileManager.ProfileManagerSortType.DATE_ASC,
            "점수높은순" to ProfileManager.ProfileManagerSortType.SCORE_DESC,
            "점수낮은순" to ProfileManager.ProfileManagerSortType.SCORE_ASC,
            "이름순" to ProfileManager.ProfileManagerSortType.NAME_ASC,
            "이름역순" to ProfileManager.ProfileManagerSortType.NAME_DESC
        )

        sortOptions.forEachIndexed { index, (label, sortType) ->
            val chip = inflater.inflate(R.layout.chip_sort, chipGroup, false) as Chip
            chip.text = label
            chip.isChecked = index == 0
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    onSortChanged(sortType)
                }
            }
            chipGroup.addView(chip)
        }
    }
}