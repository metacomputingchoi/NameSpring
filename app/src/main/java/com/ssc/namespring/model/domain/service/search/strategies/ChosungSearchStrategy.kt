// model/domain/service/search/strategies/ChosungSearchStrategy.kt
package com.ssc.namespring.model.domain.service.search.strategies

import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.common.utils.ChosungUtils
import com.ssc.namespring.model.common.utils.MixedPatternUtils
import com.ssc.namespring.model.data.source.SurnameStore
import com.ssc.namespring.model.domain.service.base.BaseSearchStrategy

class ChosungSearchStrategy(store: SurnameStore) : BaseSearchStrategy(store) {

    override fun search(query: String, results: MutableList<SurnameSearchResult>) {
        when (query.length) {
            1 -> searchSingleChosung(query, results)
            else -> searchMultipleChosung(query, results)
        }
    }

    private fun searchSingleChosung(query: String, results: MutableList<SurnameSearchResult>) {
        // 초성 매핑에서 직접 찾기
        store.chosungMapping[query]?.forEach { korean ->
            addSurnameResults(korean, results)
            if (korean.length == 1) {
                addCompoundSurnamesStartingWith(korean, results)
            }
        }
    }

    private fun searchMultipleChosung(query: String, results: MutableList<SurnameSearchResult>) {
        // 1. 단일 성씨: charTripleDict에서 초성 패턴 매칭
        store.charTripleDict.forEach { (key, info) ->
            if (key.contains("/") && key.count { it == '/' } == 1) {
                val parts = key.split("/")
                val korean = parts[0]

                if (korean.length == 1 && MixedPatternUtils.matchChosungPattern(korean, query)) {
                    results.add(SurnameSearchResult(
                        korean = korean,
                        hanja = parts[1],
                        meaning = info.integratedInfo.nameMeaning,
                        isCompound = false
                    ))
                }
            }
        }

        // 2. 복성: surnameHanjaMapping에서 초성 패턴 매칭
        store.surnameHanjaMapping.keys
            .filter { it.contains("/") && it.count { c -> c == '/' } == 1 }
            .forEach { compoundKey ->
                val parts = compoundKey.split("/")
                val korean = parts[0]
                if (korean.length > 1 && MixedPatternUtils.matchChosungPattern(korean, query)) {
                    addCompoundSurnameResult(korean, parts[1], results)
                }
            }
    }
}