// ui/compare/CompareViewModelObserver.kt
package com.ssc.namespring.ui.compare

import androidx.lifecycle.LifecycleOwner
import com.ssc.namespring.ui.compare.adapter.CompareSourceAdapter
import com.ssc.namespring.ui.compare.adapter.CompareTargetAdapter

class CompareViewModelObserver(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: CompareViewModel,
    private val sourceAdapter: CompareSourceAdapter,
    private val targetAdapter: CompareTargetAdapter,
    private val uiUpdater: CompareUIUpdater,
    private val sortManager: CompareSortManager,
    private val viewBinder: CompareActivityViewBinder
) {
    fun observeViewModel() {
        // 필터링된 즐겨찾기 목록
        viewModel.filteredFavorites.observe(lifecycleOwner) { favorites ->
            sourceAdapter.submitList(favorites)
            uiUpdater.updateEmptyView(viewBinder.emptyViewSource, favorites.isEmpty(), "source")
        }

        // 비교 대상 목록
        viewModel.comparisonList.observe(lifecycleOwner) { comparisonList ->
            targetAdapter.submitList(comparisonList)
            uiUpdater.updateEmptyView(viewBinder.emptyViewTarget, comparisonList.isEmpty(), "target")

            val selectedKeys = comparisonList.map { it.getKey() }.toSet()
            sourceAdapter.updateSelectedItems(selectedKeys)

            uiUpdater.updateCompareButton(comparisonList.size)
            uiUpdater.updateComparisonMode(comparisonList.isNotEmpty())
            uiUpdater.updateSelectionInfo(comparisonList)
        }

        // 활성 필터
        viewModel.activeFilters.observe(lifecycleOwner) { filters ->
            uiUpdater.updateFilterChips(filters)
        }

        // 활성 정렬
        viewModel.activeSorts.observe(lifecycleOwner) { sorts ->
            sortManager.updateSortChips(sorts)
        }
    }
}