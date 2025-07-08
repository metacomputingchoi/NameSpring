// model/domain/service/search/SurnameDataCollector.kt
package com.ssc.namespring.model.domain.service.search

import com.ssc.namespring.model.data.source.SurnameStore
import com.ssc.namespring.model.domain.entity.SurnameSearchResult

/**
 * 성씨 데이터 수집기
 */
class SurnameDataCollector(private val store: SurnameStore) {

    fun collectAllSurnames(): List<SurnameSearchResult> {
        val results = mutableListOf<SurnameSearchResult>()
        collectSingleSurnames(results)
        collectCompoundSurnames(results)
        return results
    }

    private fun collectSingleSurnames(results: MutableList<SurnameSearchResult>) {
        store.charTripleDict.forEach { (key, info) ->
            if (isSingleSurnameKey(key)) {
                val parts = key.split("/")
                results.add(SurnameSearchResult(
                    korean = parts[0],
                    hanja = parts[1],
                    meaning = info.integratedInfo.nameMeaning,
                    isCompound = false
                ))
            }
        }
    }

    private fun collectCompoundSurnames(results: MutableList<SurnameSearchResult>) {
        store.surnameHanjaMapping.keys
            .filter { isCompoundSurnameKey(it) }
            .forEach { compoundKey ->
                val parts = compoundKey.split("/")
                val meanings = collectMeanings(store.surnameHanjaMapping[compoundKey] ?: emptyList())
                results.add(SurnameSearchResult(
                    korean = parts[0],
                    hanja = parts[1],
                    meaning = meanings.joinToString("; ").ifEmpty { null },
                    isCompound = true
                ))
            }
    }

    private fun isSingleSurnameKey(key: String): Boolean {
        if (!key.contains("/") || key.count { it == '/' } != 1) return false
        val korean = key.split("/")[0]
        return korean.length == 1
    }

    private fun isCompoundSurnameKey(key: String): Boolean {
        if (!key.contains("/") || key.count { it == '/' } != 1) return false
        val korean = key.split("/")[0]
        return korean.length > 1
    }

    private fun collectMeanings(parts: List<String>): List<String> {
        return parts.mapNotNull { partKey ->
            store.charTripleDict[partKey]?.integratedInfo?.nameMeaning
        }
    }
}