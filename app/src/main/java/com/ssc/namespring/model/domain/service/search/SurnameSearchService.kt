// model/domain/service/search/SurnameSearchService.kt
package com.ssc.namespring.model.domain.service.search

import android.util.Log
import com.ssc.namespring.model.domain.service.interfaces.SearchService
import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.data.source.DataLoader
import com.ssc.namespring.model.data.source.SurnameStore
import com.ssc.namespring.model.domain.service.search.strategies.*

class SurnameSearchService(private val store: SurnameStore) : SearchService<SurnameSearchResult> {
    companion object {
        private const val TAG = "SurnameSearchService"
    }

    private val strategies = mapOf(
        SearchType.CHOSUNG to ChosungSearchStrategy(store),
        SearchType.KOREAN to KoreanSearchStrategy(store),
        SearchType.HANJA to HanjaSearchStrategy(store)
    )

    private val infoBuilder = SurnameInfoBuilder(store)

    override fun search(query: String): List<SurnameSearchResult> {
        if (!DataLoader.isReady()) {
            Log.e(TAG, "데이터가 아직 로드되지 않았습니다")
            return emptyList()
        }

        if (store.surnameMapping.isEmpty() || store.charTripleDict.isEmpty()) {
            Log.e(TAG, "성씨 데이터가 비어있습니다")
            return emptyList()
        }

        val results = mutableListOf<SurnameSearchResult>()
        val searchType = determineSearchType(query)

        strategies[searchType]?.search(query, results)

        return results.distinctBy { "${it.korean}/${it.hanja}" }
            .sortedWith(compareBy({ !it.isCompound }, { it.korean }))
    }

    fun getSurnameInfo(korean: String, hanja: String): SurnameInfo? {
        return infoBuilder.build(korean, hanja)
    }

    private fun determineSearchType(query: String): SearchType {
        return when {
            query.matches(Regex("[ㄱ-ㅎ]+")) -> SearchType.CHOSUNG
            query.matches(Regex("[가-힣]+")) -> SearchType.KOREAN
            else -> SearchType.HANJA
        }
    }

    private enum class SearchType {
        CHOSUNG, KOREAN, HANJA
    }
}
