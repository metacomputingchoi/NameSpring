// model/domain/service/search/strategies/KoreanSearchStrategy.kt
package com.ssc.namespring.model.domain.service.search.strategies

import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.data.source.SurnameStore
import com.ssc.namespring.model.domain.service.base.BaseSearchStrategy

class KoreanSearchStrategy(store: SurnameStore) : BaseSearchStrategy(store) {

    override fun search(query: String, results: MutableList<SurnameSearchResult>) {
        searchCompoundSurnamesExact(query, results)
        if (query.length == 1) {
            addCompoundSurnamesStartingWith(query, results)
        }
        addSurnameResults(query, results)
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
