// model/presentation/components/search/HanjaSearchInitializer.kt
package com.ssc.namespring.model.presentation.components.search

import android.view.View
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import com.ssc.namespring.model.presentation.adapter.HanjaSearchAdapter
import kotlinx.coroutines.CoroutineScope

internal class HanjaSearchInitializer {

    fun initializeComponents(
        dialogView: View,
        adapter: HanjaSearchAdapter,
        hasKoreanConstraint: Boolean,
        isChosung: Boolean,
        initialKorean: String,
        searchScope: CoroutineScope,
        nameDataService: INameDataService,
        onItemSelected: () -> Unit
    ): HanjaSearchComponents {
        adapter.onItemSelected = onItemSelected

        val uiUpdater = HanjaSearchUIUpdater(dialogView, hasKoreanConstraint, emptyList())
        val searchFilter = SearchFilterService()

        return HanjaSearchComponents(
            adapter = adapter,
            searchScope = searchScope,
            uiUpdater = uiUpdater,
            nameDataService = nameDataService,
            searchFilter = searchFilter,
            hasKoreanConstraint = hasKoreanConstraint,
            isChosung = isChosung,
            initialKorean = initialKorean
        )
    }
}

internal data class HanjaSearchComponents(
    val adapter: HanjaSearchAdapter,
    val searchScope: CoroutineScope,
    val uiUpdater: HanjaSearchUIUpdater,
    val nameDataService: INameDataService,
    val searchFilter: SearchFilterService,
    val hasKoreanConstraint: Boolean,
    val isChosung: Boolean,
    val initialKorean: String
)