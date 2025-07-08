// model/domain/service/search/strategies/HanjaSearchStrategy.kt
package com.ssc.namespring.model.domain.service.search.strategies

import android.util.Log
import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.data.source.SurnameStore
import com.ssc.namespring.model.domain.service.base.BaseSearchStrategy

class HanjaSearchStrategy(store: SurnameStore) : BaseSearchStrategy(store) {

    override fun search(query: String, results: MutableList<SurnameSearchResult>) {
        searchInCharTripleDict(query, results)
        searchInCompoundSurnames(query, results)
    }

    private fun searchInCharTripleDict(query: String, results: MutableList<SurnameSearchResult>) {
        store.charTripleDict.entries.forEach { (key, value) ->
            try {
                val hanja = value.hanjaInfo.hanja.takeIf { it.isNotEmpty() } ?: return@forEach
                val korean = value.koreanInfo.korean.takeIf { it.isNotEmpty() } ?: return@forEach

                if (hanja.contains(query)) {
                    results.add(SurnameSearchResult(
                        korean = korean,
                        hanja = hanja,
                        meaning = value.integratedInfo.nameMeaning,
                        isCompound = false
                    ))
                }
            } catch (e: Exception) {
                Log.w("IHanjaSearchStrategy", "Skipping invalid entry: $key", e)
            }
        }
    }

    private fun searchInCompoundSurnames(query: String, results: MutableList<SurnameSearchResult>) {
        store.surnameHanjaMapping.keys
            .filter { key ->
                key.contains("/") &&
                        key.split("/").getOrNull(1)?.contains(query) == true
            }
            .forEach { key ->
                val parts = key.split("/")
                if (parts.size >= 2 && parts[0].length > 1) {
                    addCompoundSurnameResult(parts[0], parts[1], results)
                }
            }
    }
}
