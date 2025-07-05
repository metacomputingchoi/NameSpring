// model/repository/surname/search/KoreanSearchStrategy.kt
package com.ssc.namespring.model.repository.surname.search

import com.ssc.namespring.model.repository.SurnameSearchResult
import com.ssc.namespring.model.repository.surname.SurnameStore

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