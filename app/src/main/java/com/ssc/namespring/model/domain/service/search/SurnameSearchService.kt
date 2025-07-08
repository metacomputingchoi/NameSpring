// model/domain/service/search/SurnameSearchService.kt
package com.ssc.namespring.model.domain.service.search

import android.util.Log
import com.ssc.namespring.model.domain.service.interfaces.SearchService
import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.data.source.DataLoader
import com.ssc.namespring.model.data.source.SurnameStore
import com.ssc.namespring.model.domain.service.search.strategies.*
import com.ssc.namespring.model.common.utils.MixedPatternUtils

/**
 * 성씨 검색 서비스
 */
class SurnameSearchService(private val store: SurnameStore) : SearchService<SurnameSearchResult> {
    companion object {
        private const val TAG = "SurnameSearchService"
    }

    private val strategies = mapOf(
        SearchType.CHOSUNG to ChosungSearchStrategy(store),
        SearchType.KOREAN to KoreanSearchStrategy(store),
        SearchType.HANJA to HanjaSearchStrategy(store),
        SearchType.MIXED to MixedPatternSearchStrategy(store)
    )

    private val infoBuilder = SurnameInfoBuilder(store)
    private val dataCollector = SurnameDataCollector(store)
    private val resultProcessor = SurnameResultProcessor()

    fun getAllSurnames(): List<SurnameSearchResult> {
        if (!validateDataReady()) return emptyList()

        val results = dataCollector.collectAllSurnames()
        return resultProcessor.processResults(results)
    }

    override fun search(query: String): List<SurnameSearchResult> {
        if (!validateDataReady() || !validateStoreData()) return emptyList()

        val results = mutableListOf<SurnameSearchResult>()
        val searchType = determineSearchType(query)

        Log.d(TAG, "검색 쿼리: '$query', 타입: $searchType")
        strategies[searchType]?.search(query, results)

        return resultProcessor.processResults(results)
    }

    fun getSurnameInfo(korean: String, hanja: String): SurnameInfo? {
        return infoBuilder.build(korean, hanja)
    }

    private fun validateDataReady(): Boolean {
        if (!DataLoader.isReady()) {
            Log.e(TAG, "데이터가 아직 로드되지 않았습니다")
            return false
        }
        return true
    }

    private fun validateStoreData(): Boolean {
        if (store.charTripleDict.isEmpty()) {
            Log.e(TAG, "성씨 데이터가 비어있습니다")
            return false
        }
        return true
    }

    private fun determineSearchType(query: String): SearchType {
        return when {
            MixedPatternUtils.containsMixedPattern(query) -> SearchType.MIXED
            query.matches(Regex("[ㄱ-ㅎ]+")) -> SearchType.CHOSUNG
            query.matches(Regex("[가-힣]+")) -> SearchType.KOREAN
            else -> SearchType.HANJA
        }
    }

    private enum class SearchType {
        CHOSUNG, KOREAN, HANJA, MIXED
    }
}