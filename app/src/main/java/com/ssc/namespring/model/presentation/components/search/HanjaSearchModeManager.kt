// model/presentation/components/search/HanjaSearchModeManager.kt
package com.ssc.namespring.model.presentation.components.search

import android.view.View
import android.widget.EditText
import com.google.android.material.chip.ChipGroup
import com.ssc.namespring.R
import com.ssc.namespring.model.presentation.components.SearchDialogManager.SearchMode

internal class HanjaSearchModeManager {

    private var currentSearchMode = SearchMode.ALL

    fun getCurrentMode() = currentSearchMode

    fun setupModeListener(
        dialogView: View,
        hasKoreanConstraint: Boolean,
        uiUpdater: HanjaSearchUIUpdater,
        onModeChanged: (SearchMode) -> Unit
    ) {
        val chipGroup = dialogView.findViewById<ChipGroup>(R.id.chipGroupSearchMode)

        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            currentSearchMode = when (checkedId) {
                R.id.chipAll -> SearchMode.ALL
                R.id.chipSound -> SearchMode.SOUND
                R.id.chipMeaning -> SearchMode.MEANING
                R.id.chipHanja -> SearchMode.HANJA
                else -> SearchMode.ALL
            }

            uiUpdater.updateSearchHint(currentSearchMode, hasKoreanConstraint)

            val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
            val currentQuery = etSearch.text?.toString()?.trim() ?: ""

            onModeChanged(currentSearchMode)
        }
    }
}