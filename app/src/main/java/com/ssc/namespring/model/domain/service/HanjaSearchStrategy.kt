// model/domain/service/HanjaSearchStrategy.kt
package com.ssc.namespring.model.domain.service

import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.data.source.SurnameStore

class HanjaSearchStrategy(store: SurnameStore) : SearchStrategy(store) {

    fun search(query: String, results: MutableList<SurnameSearchResult>) {
        searchInCharTripleDict(query, results)
        searchInCompoundSurnames(query, results)
    }

    private fun searchInCharTripleDict(query: String, results: MutableList<SurnameSearchResult>) {
        store.charTripleDict.entries.forEach { (key, value) ->
            if (value.hanjaInfo.hanja.contains(query)) {
                results.add(SurnameSearchResult(
                    korean = value.koreanInfo.korean,
                    hanja = value.hanjaInfo.hanja,
                    meaning = value.integratedInfo.nameMeaning,
                    isCompound = false
                ))
            }
        }
    }

    private fun searchInCompoundSurnames(query: String, results: MutableList<SurnameSearchResult>) {
        store.surnameHanjaMapping.keys
            .filter { it.contains("/") && it.split("/")[1].contains(query) }
            .forEach { key ->
                val parts = key.split("/")
                if (parts[0].length > 1) {
                    addCompoundSurnameResult(parts[0], parts[1], results)
                }
            }
    }
}