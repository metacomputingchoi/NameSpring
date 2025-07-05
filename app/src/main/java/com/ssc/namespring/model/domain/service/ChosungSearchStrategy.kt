// model/domain/service/ChosungSearchStrategy.kt
package com.ssc.namespring.model.domain.service

import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.common.utils.ChosungUtils
import com.ssc.namespring.model.data.source.SurnameStore

class ChosungSearchStrategy(store: SurnameStore) : SearchStrategy(store) {

    fun search(query: String, results: MutableList<SurnameSearchResult>) {
        when (query.length) {
            1 -> searchSingleChosung(query, results)
            2 -> searchDoubleChosung(query, results)
        }
    }

    private fun searchSingleChosung(query: String, results: MutableList<SurnameSearchResult>) {
        store.chosungMapping[query]?.forEach { korean ->
            addSurnameResults(korean, results)
            addCompoundSurnamesStartingWith(korean, results)
        }
    }

    private fun searchDoubleChosung(query: String, results: MutableList<SurnameSearchResult>) {
        if (query.length < 2) return

        val firstChosung = query[0].toString()
        val secondChosung = query[1].toString()

        store.surnameHanjaMapping.keys
            .filter { it.contains("/") && it.count { c -> c == '/' } == 1 }
            .forEach { compoundKey ->
                val parts = compoundKey.split("/")
                val korean = parts[0]
                if (korean.length == 2 &&
                    ChosungUtils.getChosung(korean[0].toString()) == firstChosung &&
                    ChosungUtils.getChosung(korean[1].toString()) == secondChosung) {
                    addCompoundSurnameResult(korean, parts[1], results)
                }
            }
    }
}