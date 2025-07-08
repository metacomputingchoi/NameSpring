// model/presentation/components/search/HanjaSearchCoordinator.kt
package com.ssc.namespring.model.presentation.components.search

import android.util.Log
import android.view.View
import android.widget.EditText
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.HanjaSearchResult
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import com.ssc.namespring.model.presentation.adapter.HanjaSearchAdapter
import com.ssc.namespring.model.presentation.components.SearchDialogManager.SearchMode
import kotlinx.coroutines.*

internal class HanjaSearchCoordinator {

    private lateinit var components: HanjaSearchComponents
    private lateinit var queryHandler: HanjaSearchQueryHandler
    private lateinit var modeManager: HanjaSearchModeManager
    private lateinit var resultLoader: HanjaSearchResultLoader

    private var baseResults: List<HanjaSearchResult> = emptyList()

    fun initialize(
        dialogView: View,
        adapter: HanjaSearchAdapter,
        hasKoreanConstraint: Boolean,
        isChosung: Boolean,
        initialKorean: String,
        searchScope: CoroutineScope,
        nameDataService: INameDataService,
        onItemSelected: () -> Unit
    ) {
        // 컴포넌트 초기화
        val initializer = HanjaSearchInitializer()
        components = initializer.initializeComponents(
            dialogView, adapter, hasKoreanConstraint, isChosung,
            initialKorean, searchScope, nameDataService, onItemSelected
        )

        // 핸들러들 초기화
        queryHandler = HanjaSearchQueryHandler(searchScope) { query ->
            performSearch(query)
        }
        modeManager = HanjaSearchModeManager()
        resultLoader = HanjaSearchResultLoader()

        // 리스너 설정
        setupSearchListeners(dialogView)
        setupSearchModeListener(dialogView)
    }

    fun loadBaseResults() {
        components.searchScope.launch {
            baseResults = resultLoader.loadBaseResults(
                components.nameDataService,
                components.hasKoreanConstraint,
                components.isChosung,
                components.initialKorean
            )

            // UI 업데이터에 베이스 결과 설정
            components.uiUpdater.baseResults = baseResults

            performSearch("")
        }
    }

    private fun setupSearchListeners(dialogView: View) {
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
        queryHandler.setupSearchListener(etSearch)
    }

    private fun setupSearchModeListener(dialogView: View) {
        modeManager.setupModeListener(
            dialogView,
            components.hasKoreanConstraint,
            components.uiUpdater
        ) { _ ->
            val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
            val currentQuery = etSearch.text?.toString()?.trim() ?: ""
            queryHandler.triggerSearch(currentQuery)
        }
    }

    private suspend fun performSearch(query: String) {
        val currentMode = modeManager.getCurrentMode()
        Log.d("HanjaSearchCoordinator", "검색 수행: query='$query', mode=$currentMode")

        val results = withContext(Dispatchers.IO) {
            components.searchFilter.filterResults(baseResults, query, currentMode)
        }

        withContext(Dispatchers.Main) {
            components.adapter.submitList(results)
            components.uiUpdater.updateResults(results)
        }
    }
}