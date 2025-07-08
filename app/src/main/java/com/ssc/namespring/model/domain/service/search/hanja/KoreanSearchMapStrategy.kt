// model/domain/service/search/hanja/KoreanSearchMapStrategy.kt
package com.ssc.namespring.model.domain.service.search.hanja

import android.util.Log
import com.ssc.namespring.model.data.mapper.OptimizedMapping
import com.ssc.namespring.model.data.mapper.HanjaInfo

internal class KoreanSearchMapStrategy(
    private val optimizedMapping: OptimizedMapping
) : IHanjaSearchStrategy {

    companion object {
        private const val TAG = "KoreanSearchMapStrategy"
    }

    override fun search(query: String): List<HanjaInfo> {
        Log.d(TAG, "한글 검색 모드: $query")

        val exactMatch = optimizedMapping.koreanToHanjaInfo[query] ?: emptyList()

        // 부분 일치 검색
        if (query.length >= 2 && exactMatch.isEmpty()) {
            val partialMatches = mutableListOf<HanjaInfo>()
            optimizedMapping.koreanToHanjaInfo.forEach { (korean, hanjaList) ->
                if (korean.contains(query)) {
                    partialMatches.addAll(hanjaList)
                }
            }
            return partialMatches.distinctBy { it.tripleKey }
        }

        return exactMatch
    }
}