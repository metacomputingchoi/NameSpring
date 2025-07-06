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

class SurnameSearchService(private val store: SurnameStore) : SearchService<SurnameSearchResult> {
    companion object {
        private const val TAG = "SurnameSearchService"
    }

    private val strategies = mapOf(
        SearchType.CHOSUNG to ChosungSearchStrategy(store),
        SearchType.KOREAN to KoreanSearchStrategy(store),
        SearchType.HANJA to HanjaSearchStrategy(store),
        SearchType.MIXED to MixedPatternSearchStrategy(store)  // 새로운 전략 추가
    )

    private val infoBuilder = SurnameInfoBuilder(store)

    fun getAllSurnames(): List<SurnameSearchResult> {
        if (!DataLoader.isReady()) {
            Log.e(TAG, "데이터가 아직 로드되지 않았습니다")
            return emptyList()
        }

        val results = mutableListOf<SurnameSearchResult>()

        // 1. 단일 성씨: charTripleDict에서 가져오기
        store.charTripleDict.forEach { (key, info) ->
            if (key.contains("/") && key.count { it == '/' } == 1) {
                val parts = key.split("/")
                val korean = parts[0]
                val hanja = parts[1]

                if (korean.length == 1) {  // 단일 성씨만
                    results.add(SurnameSearchResult(
                        korean = korean,
                        hanja = hanja,
                        meaning = info.integratedInfo.nameMeaning,
                        isCompound = false
                    ))
                }
            }
        }

        // 2. 복성: surnameHanjaMapping에서 가져오기
        store.surnameHanjaMapping.keys
            .filter { it.contains("/") && it.count { c -> c == '/' } == 1 }
            .forEach { compoundKey ->
                val parts = compoundKey.split("/")
                val korean = parts[0]
                val hanja = parts[1]

                if (korean.length > 1) {  // 복성만
                    val meanings = collectMeanings(store.surnameHanjaMapping[compoundKey] ?: emptyList())
                    results.add(SurnameSearchResult(
                        korean = korean,
                        hanja = hanja,
                        meaning = meanings.joinToString("; ").ifEmpty { null }, // 세미콜론으로 구분
                        isCompound = true
                    ))
                }
            }

        return results.distinctBy { "${it.korean}/${it.hanja}" }
            .sortedWith(compareBy({ !it.isCompound }, { it.korean }))
    }

    override fun search(query: String): List<SurnameSearchResult> {
        if (!DataLoader.isReady()) {
            Log.e(TAG, "데이터가 아직 로드되지 않았습니다")
            return emptyList()
        }

        if (store.charTripleDict.isEmpty()) {
            Log.e(TAG, "성씨 데이터가 비어있습니다")
            return emptyList()
        }

        val results = mutableListOf<SurnameSearchResult>()
        val searchType = determineSearchType(query)

        Log.d(TAG, "검색 쿼리: '$query', 타입: $searchType")

        strategies[searchType]?.search(query, results)

        return results.distinctBy { "${it.korean}/${it.hanja}" }
            .sortedWith(compareBy({ !it.isCompound }, { it.korean }))
    }

    fun getSurnameInfo(korean: String, hanja: String): SurnameInfo? {
        return infoBuilder.build(korean, hanja)
    }

    private fun collectMeanings(parts: List<String>): List<String> {
        val meanings = mutableListOf<String>()
        parts.forEach { partKey ->
            store.charTripleDict[partKey]?.integratedInfo?.nameMeaning?.let {
                meanings.add(it)
            }
        }
        return meanings
    }

    private fun determineSearchType(query: String): SearchType {
        return when {
            // 혼합 패턴 확인을 먼저
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