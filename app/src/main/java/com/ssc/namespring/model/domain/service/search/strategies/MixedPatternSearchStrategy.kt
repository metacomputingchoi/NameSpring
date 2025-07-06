// model/domain/service/search/strategies/MixedPatternSearchStrategy.kt
package com.ssc.namespring.model.domain.service.search.strategies

import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.data.source.SurnameStore
import com.ssc.namespring.model.domain.service.base.BaseSearchStrategy
import com.ssc.namespring.model.common.utils.MixedPatternUtils

class MixedPatternSearchStrategy(store: SurnameStore) : BaseSearchStrategy(store) {

    override fun search(query: String, results: MutableList<SurnameSearchResult>) {
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
            .forEach { compoundKey ->
                val parts = compoundKey.split("/")
                val korean = parts[0]
                if (korean.length > 1 && MixedPatternUtils.matchMixedPattern(korean, query)) {
                    addCompoundSurnameResult(korean, parts[1], results)
                }
            }
    }
}