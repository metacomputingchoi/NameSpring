// model/domain/service/search/NameDataSearchService.kt
package com.ssc.namespring.model.domain.service.search

import android.util.Log
import com.ssc.namespring.model.domain.service.interfaces.SearchService
import com.ssc.namespring.model.domain.entity.HanjaSearchResult
import com.ssc.namespring.model.data.mapper.OptimizedMapping
import com.ssc.namespring.model.data.mapper.HanjaInfo
import com.ssc.namespring.model.domain.service.search.hanja.*

class NameDataSearchService : SearchService<HanjaSearchResult> {
    companion object {
        private const val TAG = "NameDataSearchService"
    }

    private lateinit var optimizedMapping: OptimizedMapping
    private lateinit var queryRouter: HanjaQueryRouter
    private lateinit var resultConverter: HanjaResultConverter

    fun initialize(mapping: OptimizedMapping) {
        this.optimizedMapping = mapping
        this.queryRouter = HanjaQueryRouter(mapping)
        this.resultConverter = HanjaResultConverter()
    }

    fun getAllHanja(): List<HanjaSearchResult> {
        if (!::optimizedMapping.isInitialized) {
            Log.e(TAG, "NameData가 초기화되지 않았습니다")
            return emptyList()
        }

        val allHanjaInfo = mutableListOf<HanjaInfo>()
        optimizedMapping.koreanToHanjaInfo.values.forEach { hanjaList ->
            allHanjaInfo.addAll(hanjaList)
        }

        return allHanjaInfo
            .distinctBy { it.tripleKey }
            .sortedBy { it.korean }
            .map { resultConverter.convert(it) }
    }

    override fun search(query: String): List<HanjaSearchResult> {
        val normalizedQuery = query.trim()
        Log.d(TAG, "검색 쿼리: '$normalizedQuery'")

        if (normalizedQuery.isEmpty()) {
            return getAllHanja()
        }

        val results = queryRouter.route(normalizedQuery)
        Log.d(TAG, "검색 결과: ${results.size}개")

        return results.map { resultConverter.convert(it) }
    }

    fun searchByMeaning(query: String): List<HanjaSearchResult> {
        if (!::optimizedMapping.isInitialized) {
            Log.e(TAG, "NameData가 초기화되지 않았습니다")
            return emptyList()
        }

        if (query.isEmpty()) {
            return emptyList()
        }

        val strategy = MeaningSearchStrategy(optimizedMapping)
        return strategy.search(query).map { resultConverter.convert(it) }
    }

    fun searchByHanja(query: String): List<HanjaSearchResult> {
        if (!::optimizedMapping.isInitialized) {
            Log.e(TAG, "NameData가 초기화되지 않았습니다")
            return emptyList()
        }

        if (query.isEmpty()) {
            return emptyList()
        }

        val strategy = HanjaCharSearchStrategy(optimizedMapping)
        return strategy.search(query).map { resultConverter.convert(it) }
    }
}