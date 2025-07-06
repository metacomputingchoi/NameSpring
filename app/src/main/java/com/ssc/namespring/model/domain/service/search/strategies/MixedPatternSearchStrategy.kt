// model/domain/service/search/strategies/MixedPatternSearchStrategy.kt
package com.ssc.namespring.model.domain.service.search.strategies

import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.data.source.SurnameStore
import com.ssc.namespring.model.domain.service.base.BaseSearchStrategy
import com.ssc.namespring.model.common.utils.MixedPatternUtils

class MixedPatternSearchStrategy(store: SurnameStore) : BaseSearchStrategy(store) {

    override fun search(query: String, results: MutableList<SurnameSearchResult>) {
        // 단일 성씨 검색
        store.surnameMapping.keys.forEach { korean ->
            if (MixedPatternUtils.matchMixedPattern(korean, query)) {
                addSurnameResults(korean, results)
            }
        }

        // 복성 검색
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