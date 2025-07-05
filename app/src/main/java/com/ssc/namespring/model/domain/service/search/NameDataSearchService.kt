// model/domain/service/search/NameDataSearchService.kt
package com.ssc.namespring.model.domain.service.search

import android.util.Log
import com.ssc.namespring.model.domain.service.interfaces.SearchService
import com.ssc.namespring.model.domain.entity.HanjaSearchResult
import com.ssc.namespring.model.data.mapper.OptimizedMapping
import com.ssc.namespring.model.data.mapper.HanjaInfo

class NameDataSearchService : SearchService<HanjaSearchResult> {
    companion object {
        private const val TAG = "NameDataSearchService"
    }

    private lateinit var optimizedMapping: OptimizedMapping

    fun initialize(mapping: OptimizedMapping) {
        this.optimizedMapping = mapping
    }

    override fun search(query: String): List<HanjaSearchResult> {
        val normalizedQuery = query.trim()
        Log.d(TAG, "검색 쿼리: '$normalizedQuery'")

        val results = when {
            normalizedQuery.matches(Regex("^[ㄱ-ㅎ]$")) -> searchByChosung(normalizedQuery)
            normalizedQuery.matches(Regex("^[가-힣]$")) -> searchByKorean(normalizedQuery)
            normalizedQuery.length == 1 -> searchByHanja(normalizedQuery)
            normalizedQuery.length >= 2 -> searchByMeaning(normalizedQuery)
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

    private fun searchByChosung(query: String): List<HanjaInfo> {
        Log.d(TAG, "초성 검색 모드")
        return optimizedMapping.chosungToHanjaInfo[query] ?: emptyList()
    }

    private fun searchByKorean(query: String): List<HanjaInfo> {
        Log.d(TAG, "한글 검색 모드")
        return optimizedMapping.koreanToHanjaInfo[query] ?: emptyList()
    }

    private fun searchByHanja(query: String): List<HanjaInfo> {
        Log.d(TAG, "한자 검색 모드")
        return optimizedMapping.hanjaToHanjaInfo[query] ?: emptyList()
    }

    private fun searchByMeaning(query: String): List<HanjaInfo> {
        Log.d(TAG, "뜻 검색 모드")
        val results = mutableListOf<HanjaInfo>()
        optimizedMapping.meaningSearchIndex.forEach { (word, hanjaList) ->
            if (word.contains(query)) {
                results.addAll(hanjaList)
            }
        }
        return results.distinctBy { it.tripleKey }
    }
}
