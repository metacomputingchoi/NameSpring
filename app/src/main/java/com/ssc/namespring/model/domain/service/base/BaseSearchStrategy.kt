// model/domain/service/base/BaseSearchStrategy.kt
package com.ssc.namespring.model.domain.service.base

import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.data.source.SurnameStore

abstract class BaseSearchStrategy(protected val store: SurnameStore) {
    abstract fun search(query: String, results: MutableList<SurnameSearchResult>)

    protected fun addSurnameResults(korean: String, results: MutableList<SurnameSearchResult>) {
        store.surnameMapping[korean]?.forEach { hanja ->
            val key = "$korean/$hanja"
            store.charTripleDict[key]?.let { info ->
                results.add(SurnameSearchResult(
                    korean = korean,
                    hanja = hanja,
                    meaning = info.integratedInfo.nameMeaning,
                    isCompound = false
                ))
            }
        }
    }

    protected fun addCompoundSurnameResult(korean: String, hanja: String, results: MutableList<SurnameSearchResult>) {
        val compoundKey = "$korean/$hanja"
        store.surnameHanjaMapping[compoundKey]?.let { parts ->
            if (parts.size >= 2) {
                val meanings = collectMeanings(parts)
                results.add(SurnameSearchResult(
                    korean = korean,
                    hanja = hanja,
                    meaning = meanings.joinToString(" ").ifEmpty { null },
                    isCompound = true
                ))
            }
        }
    }

    protected fun collectMeanings(parts: List<String>): List<String> {
        val meanings = mutableListOf<String>()
        parts.forEach { partKey ->
            store.charTripleDict[partKey]?.integratedInfo?.nameMeaning?.let {
                meanings.add(it)
            }
        }
        return meanings
    }

    protected fun addCompoundSurnamesStartingWith(korean: String, results: MutableList<SurnameSearchResult>) {
        store.surnameHanjaMapping.keys
            .filter { it.contains("/") && it.startsWith(korean) && it.count { c -> c == '/' } == 1 }
            .forEach { compoundKey ->
                val parts = compoundKey.split("/")
                if (parts[0].length > 1) {
                    addCompoundSurnameResult(parts[0], parts[1], results)
                }
            }
    }
}
