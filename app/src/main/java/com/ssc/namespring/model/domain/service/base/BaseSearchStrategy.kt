// model/domain/service/base/BaseSearchStrategy.kt
package com.ssc.namespring.model.domain.service.base

import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.data.source.SurnameStore

abstract class BaseSearchStrategy(protected val store: SurnameStore) {
    abstract fun search(query: String, results: MutableList<SurnameSearchResult>)

    protected fun addSurnameResults(korean: String, results: MutableList<SurnameSearchResult>) {
        // charTripleDict에서 직접 검색
        store.charTripleDict.forEach { (key, info) ->
            if (key.startsWith("$korean/") && key.count { it == '/' } == 1) {
                val hanja = key.split("/")[1]
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
                    meaning = meanings.joinToString("; ").ifEmpty { null }, // 세미콜론으로 구분
                    isCompound = true
                ))
            }
        }
    }

    protected fun collectMeanings(parts: List<String>): List<String> {
        val meanings = mutableListOf<String>()
        parts.forEach { partKey ->
            store.charTripleDict[partKey]?.integratedInfo?.let { info ->
                val meaning = info.nameMeaning ?: ""
                val korean = partKey.split("/")[0]
                val hanja = partKey.split("/")[1]
                // 각 글자의 정보를 "한글(한자): 뜻" 형식으로 저장
                if (meaning.isNotEmpty()) {
                    meanings.add("$meaning")
                } else {
                    meanings.add("뜻없음")
                }
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
