// model/domain/service/search/hanja/MeaningSearchStrategy.kt
package com.ssc.namespring.model.domain.service.search.hanja

import android.util.Log
import com.ssc.namespring.model.data.mapper.OptimizedMapping
import com.ssc.namespring.model.data.mapper.HanjaInfo

internal class MeaningSearchStrategy(
    private val optimizedMapping: OptimizedMapping
) : HanjaSearchStrategy {

    companion object {
        private const val TAG = "MeaningSearchStrategy"
    }

    override fun search(query: String): List<HanjaInfo> {
        Log.d(TAG, "뜻 검색 모드: $query")

        val results = mutableListOf<HanjaInfo>()

        // meaningSearchIndex에서 검색
        optimizedMapping.meaningSearchIndex.forEach { (word, hanjaList) ->
            if (word.contains(query, ignoreCase = true)) {
                results.addAll(hanjaList)
            }
        }

        // 직접 meaning 필드에서도 검색 (meaningSearchIndex에 없는 경우 대비)
        optimizedMapping.koreanToHanjaInfo.values.forEach { hanjaList ->
            hanjaList.forEach { info ->
                val meaning = info.meaning ?: ""
                if (meaning.contains(query, ignoreCase = true) &&
                    results.none { it.tripleKey == info.tripleKey }) {
                    results.add(info)
                }
            }
        }

        Log.d(TAG, "뜻 검색 결과: ${results.size}개")

        return results.distinctBy { it.tripleKey }
            .sortedBy { it.korean }
    }
}