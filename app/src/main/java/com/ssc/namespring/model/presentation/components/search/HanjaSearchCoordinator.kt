// model/presentation/components/search/HanjaSearchCoordinator.kt
package com.ssc.namespring.model.presentation.components.search

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import com.google.android.material.chip.ChipGroup
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.HanjaSearchResult
import com.ssc.namespring.model.domain.entity.NameData
import com.ssc.namespring.model.presentation.adapter.HanjaSearchAdapter
import com.ssc.namespring.model.presentation.components.SearchDialogManager.SearchMode
import kotlinx.coroutines.*

internal class HanjaSearchCoordinator {

    private lateinit var adapter: HanjaSearchAdapter
    private lateinit var searchScope: CoroutineScope
    private lateinit var uiUpdater: HanjaSearchUIUpdater
    private val searchFilter = SearchFilterService()

    private var baseResults: List<HanjaSearchResult> = emptyList()
    private var currentSearchMode = SearchMode.ALL
    private var searchJob: Job? = null
    private var hasKoreanConstraint = false
    private var isChosung = false
    private var initialKorean = ""

    fun initialize(
        dialogView: View,
        adapter: HanjaSearchAdapter,
        hasKoreanConstraint: Boolean,
        isChosung: Boolean,
        initialKorean: String,
        searchScope: CoroutineScope,
        onItemSelected: () -> Unit
    ) {
        this.adapter = adapter
        this.searchScope = searchScope
        this.hasKoreanConstraint = hasKoreanConstraint
        this.isChosung = isChosung
        this.initialKorean = initialKorean
        this.uiUpdater = HanjaSearchUIUpdater(dialogView, hasKoreanConstraint, baseResults)

        adapter.onItemSelected = onItemSelected

        setupSearchListeners(dialogView)
        setupSearchModeListener(dialogView)
    }

    fun loadBaseResults() {
        searchScope.launch {
            baseResults = withContext(Dispatchers.IO) {
                if (hasKoreanConstraint) {
                    if (isChosung) {
                        NameData.searchHanja(initialKorean)
                    } else {
                        NameData.searchHanja(initialKorean)
                            .filter { it.korean == initialKorean }
                    }
                } else {
                    NameData.getAllHanja()
                }
            }
            Log.d("HanjaSearchCoordinator", "베이스 결과: ${baseResults.size}개")
            performSearch("")
        }
    }

    private fun setupSearchListeners(dialogView: View) {
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                searchJob?.cancel()
                searchJob = searchScope.launch {
                    delay(300)
                    performSearch(query)
                }
            }
        })
    }

    private fun setupSearchModeListener(dialogView: View) {
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

            searchJob?.cancel()
            searchJob = searchScope.launch { performSearch(currentQuery) }
        }
    }

    private suspend fun performSearch(query: String) {
        Log.d("HanjaSearchCoordinator", "검색 수행: query='$query', mode=$currentSearchMode")

        val results = withContext(Dispatchers.IO) {
            searchFilter.filterResults(baseResults, query, currentSearchMode)
        }

        withContext(Dispatchers.Main) {
            adapter.submitList(results)
            uiUpdater.updateResults(results)
        }
    }
}