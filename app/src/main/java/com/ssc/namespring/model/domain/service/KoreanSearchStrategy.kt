// model/domain/service/KoreanSearchStrategy.kt
package com.ssc.namespring.model.domain.service

import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.data.source.SurnameStore

class KoreanSearchStrategy(store: SurnameStore) : SearchStrategy(store) {

    fun search(query: String, results: MutableList<SurnameSearchResult>) {
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