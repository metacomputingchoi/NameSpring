// model/domain/service/search/hanja/HanjaCharSearchStrategy.kt
package com.ssc.namespring.model.domain.service.search.hanja

import android.util.Log
import com.ssc.namespring.model.data.mapper.OptimizedMapping
import com.ssc.namespring.model.data.mapper.HanjaInfo

internal class HanjaCharSearchStrategy(
    private val optimizedMapping: OptimizedMapping
) : IHanjaSearchStrategy {

    companion object {
        private const val TAG = "HanjaCharSearchStrategy"
    }

    override fun search(query: String): List<HanjaInfo> {
        Log.d(TAG, "한자 검색 모드: $query")

        // 단일 한자 검색
        if (query.length == 1) {
            return optimizedMapping.hanjaToHanjaInfo[query] ?: emptyList()
        }

        // 한자가 포함된 복합 검색
        val results = mutableListOf<HanjaInfo>()
        optimizedMapping.hanjaToHanjaInfo.forEach { (hanja, infoList) ->
            if (query.contains(hanja)) {
                results.addAll(infoList)
            }
        }

        return results.distinctBy { it.tripleKey }
    }
}