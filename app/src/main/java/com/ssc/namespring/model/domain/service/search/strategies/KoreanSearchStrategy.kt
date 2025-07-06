// model/domain/service/search/strategies/KoreanSearchStrategy.kt
package com.ssc.namespring.model.domain.service.search.strategies

import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.data.source.SurnameStore
import com.ssc.namespring.model.domain.service.base.BaseSearchStrategy
import com.ssc.namespring.model.common.utils.MixedPatternUtils

class KoreanSearchStrategy(store: SurnameStore) : BaseSearchStrategy(store) {

    override fun search(query: String, results: MutableList<SurnameSearchResult>) {
        // 혼합 패턴 검색
        if (MixedPatternUtils.containsMixedPattern(query)) {
            searchWithMixedPattern(query, results)
            return
        }

        // charTripleDict에서 직접 검색
        store.charTripleDict.forEach { (key, info) ->
            if (key.contains("/") && key.count { it == '/' } == 1) {
                val parts = key.split("/")
                val korean = parts[0]

                // 단일 성씨이고 쿼리와 일치하는 경우
                if (korean.length == 1 && korean == query) {
                    results.add(SurnameSearchResult(
                        korean = korean,
                        hanja = parts[1],
                        meaning = info.integratedInfo.nameMeaning,
                        isCompound = false
                    ))
                }
            }
        }

        // 복성 검색 (기존 코드)
        searchCompoundSurnamesExact(query, results)
    }

    private fun searchWithMixedPattern(query: String, results: MutableList<SurnameSearchResult>) {
        // 1. 단일 성씨 검색: charTripleDict에서
        store.charTripleDict.forEach { (key, info) ->
            if (key.contains("/") && key.count { it == '/' } == 1) {
                val parts = key.split("/")
                val korean = parts[0]

                if (korean.length == 1 && MixedPatternUtils.matchMixedPattern(korean, query)) {
                    results.add(SurnameSearchResult(
                        korean = korean,
                        hanja = parts[1],
                        meaning = info.integratedInfo.nameMeaning,
                        isCompound = false
                    ))
                }
            }
        }

        // 2. 복성 검색: surnameHanjaMapping에서
        store.surnameHanjaMapping.keys
            .filter { it.contains("/") && it.count { c -> c == '/' } == 1 }
            .forEach { key ->
                val parts = key.split("/")
                val korean = parts[0]
                if (korean.length > 1 && MixedPatternUtils.matchMixedPattern(korean, query)) {
                    addCompoundSurnameResult(korean, parts[1], results)
                }
            }
    }

    private fun searchCompoundSurnamesExact(query: String, results: MutableList<SurnameSearchResult>) {
        store.surnameHanjaMapping.keys
            .filter { it.startsWith("$query/") }
            .forEach { key ->
                val parts = key.split("/")
                if (parts[0] == query && query.length > 1) {
                    addCompoundSurnameResult(query, parts[1], results)
                }
            }
    }
}
