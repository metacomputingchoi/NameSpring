// model/repository/NameDataSearcher.kt
package com.ssc.namespring.model.repository

import android.util.Log

class NameDataSearcher {
    companion object {
        private const val TAG = "NameDataSearcher"
    }

    fun searchHanja(query: String, optimizedMapping: NameData.OptimizedMapping): List<HanjaSearchResult> {
        val normalizedQuery = query.trim()
        Log.d(TAG, "검색 쿼리: '$normalizedQuery'")

        val results = when {
            normalizedQuery.matches(Regex("^[ㄱ-ㅎ]$")) -> searchByChosung(normalizedQuery, optimizedMapping)
            normalizedQuery.matches(Regex("^[가-힣]$")) -> searchByKorean(normalizedQuery, optimizedMapping)
            normalizedQuery.length == 1 -> searchByHanja(normalizedQuery, optimizedMapping)
            normalizedQuery.length >= 2 -> searchByMeaning(normalizedQuery, optimizedMapping)
            else -> emptyList()
        }

        return results.map { info ->
            HanjaSearchResult(
                korean = info.korean,
                hanja = info.hanja,
                meaning = info.meaning,
                ohaeng = info.ohaeng,
                strokes = info.strokes,
                soundCount = 1,
                tripleKey = info.tripleKey
            )
        }
    }

    private fun searchByChosung(query: String, mapping: NameData.OptimizedMapping): List<NameData.HanjaInfo> {
        Log.d(TAG, "초성 검색 모드")
        return mapping.chosungToHanjaInfo[query] ?: emptyList()
    }

    private fun searchByKorean(query: String, mapping: NameData.OptimizedMapping): List<NameData.HanjaInfo> {
        Log.d(TAG, "한글 검색 모드")
        return mapping.koreanToHanjaInfo[query] ?: emptyList()
    }

    private fun searchByHanja(query: String, mapping: NameData.OptimizedMapping): List<NameData.HanjaInfo> {
        Log.d(TAG, "한자 검색 모드")
        return mapping.hanjaToHanjaInfo[query] ?: emptyList()
    }

    private fun searchByMeaning(query: String, mapping: NameData.OptimizedMapping): List<NameData.HanjaInfo> {
        Log.d(TAG, "뜻 검색 모드")
        val results = mutableListOf<NameData.HanjaInfo>()
        mapping.meaningSearchIndex.forEach { (word, hanjaList) ->
            if (word.contains(query)) {
                results.addAll(hanjaList)
            }
        }
        return results.distinctBy { it.tripleKey }
    }
}