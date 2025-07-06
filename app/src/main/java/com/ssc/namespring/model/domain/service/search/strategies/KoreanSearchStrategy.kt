// model/domain/service/search/strategies/KoreanSearchStrategy.kt
package com.ssc.namespring.model.domain.service.search.strategies

import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.data.source.SurnameStore
import com.ssc.namespring.model.domain.service.base.BaseSearchStrategy
import com.ssc.namespring.model.common.utils.MixedPatternUtils

class KoreanSearchStrategy(store: SurnameStore) : BaseSearchStrategy(store) {

    override fun search(query: String, results: MutableList<SurnameSearchResult>) {
        // 혼합 패턴 검색 (한글+초성)
        if (MixedPatternUtils.containsMixedPattern(query)) {
            searchWithMixedPattern(query, results)
            return
        }

        // 기존 로직
        searchCompoundSurnamesExact(query, results)
        if (query.length == 1) {
            addCompoundSurnamesStartingWith(query, results)
        }
        addSurnameResults(query, results)
    }

    private fun searchWithMixedPattern(query: String, results: MutableList<SurnameSearchResult>) {
        // 단일 성씨 검색
        store.surnameMapping.keys.forEach { korean ->
            if (MixedPatternUtils.matchMixedPattern(korean, query)) {
                addSurnameResults(korean, results)
            }
        }

        // 복성 검색
        store.surnameHanjaMapping.keys
            .filter { it.contains("/") }
            .forEach { key ->
                val korean = key.split("/")[0]
                if (MixedPatternUtils.matchMixedPattern(korean, query)) {
                    val hanja = key.split("/")[1]
                    addCompoundSurnameResult(korean, hanja, results)
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
