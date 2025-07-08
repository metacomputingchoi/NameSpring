// model/domain/service/search/hanja/ChosungSearchMapStrategy.kt
package com.ssc.namespring.model.domain.service.search.hanja

import android.util.Log
import com.ssc.namespring.model.data.mapper.OptimizedMapping
import com.ssc.namespring.model.data.mapper.HanjaInfo

internal class ChosungSearchMapStrategy(
    private val optimizedMapping: OptimizedMapping
) : IHanjaSearchStrategy {

    companion object {
        private const val TAG = "ChosungSearchMapStrategy"
    }

    override fun search(query: String): List<HanjaInfo> {
        Log.d(TAG, "초성 검색 모드: $query")

        if (query.length == 1) {
            return optimizedMapping.chosungToHanjaInfo[query] ?: emptyList()
        }

        val results = mutableListOf<HanjaInfo>()
        optimizedMapping.koreanToHanjaInfo.forEach { (korean, hanjaList) ->
            if (matchesChosungPattern(korean, query)) {
                results.addAll(hanjaList)
            }
        }
        return results.distinctBy { it.tripleKey }
    }

    private fun matchesChosungPattern(text: String, pattern: String): Boolean {
        if (text.length < pattern.length) return false

        for (i in pattern.indices) {
            val textChar = text.getOrNull(i) ?: return false
            val textCharChosung = HanjaSearchUtils.getChosung(textChar)
            if (textCharChosung != pattern[i].toString()) {
                return false
            }
        }
        return true
    }
}