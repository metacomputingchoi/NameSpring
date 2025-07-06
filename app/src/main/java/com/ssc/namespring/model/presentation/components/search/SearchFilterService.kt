// model/presentation/components/search/SearchFilterService.kt
package com.ssc.namespring.model.presentation.components.search

import com.ssc.namespring.model.domain.entity.HanjaSearchResult
import com.ssc.namespring.model.presentation.components.SearchDialogManager.SearchMode

internal class SearchFilterService {

    private val resultMatcher = SearchResultMatcher()

    fun filterResults(
        baseResults: List<HanjaSearchResult>,
        query: String,
        searchMode: SearchMode
    ): List<HanjaSearchResult> {
        if (query.isEmpty()) {
            return baseResults
        }

        return when (searchMode) {
            SearchMode.ALL -> searchAllInResults(baseResults, query)
            SearchMode.SOUND -> searchSoundInResults(baseResults, query)
            SearchMode.MEANING -> searchMeaningInResults(baseResults, query)
            SearchMode.HANJA -> searchHanjaInResults(baseResults, query)
        }
    }

    private fun searchAllInResults(
        baseResults: List<HanjaSearchResult>,
        query: String
    ): List<HanjaSearchResult> {
        return baseResults.filter { result ->
            resultMatcher.checkSound(result.korean, query) ||
                    resultMatcher.checkMeaning(result.meaning, query) ||
                    resultMatcher.checkHanja(result.hanja, query) ||
                    resultMatcher.checkStroke(result.strokes, query)
        }
    }

    private fun searchSoundInResults(
        baseResults: List<HanjaSearchResult>,
        query: String
    ): List<HanjaSearchResult> {
        return baseResults.filter { result ->
            resultMatcher.checkSound(result.korean, query)
        }
    }

    private fun searchMeaningInResults(
        baseResults: List<HanjaSearchResult>,
        query: String
    ): List<HanjaSearchResult> {
        return baseResults.filter { result ->
            resultMatcher.checkMeaning(result.meaning, query)
        }
    }

    private fun searchHanjaInResults(
        baseResults: List<HanjaSearchResult>,
        query: String
    ): List<HanjaSearchResult> {
        return baseResults.filter { result ->
            resultMatcher.checkHanja(result.hanja, query) ||
                    resultMatcher.checkStroke(result.strokes, query)
        }
    }
}