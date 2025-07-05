// model/domain/service/SurnameSearcher.kt
package com.ssc.namespring.model.domain.service

import android.util.Log
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.data.source.DataLoader
import com.ssc.namespring.model.domain.entity.SurnameSearchResult
import com.ssc.namespring.model.data.source.SurnameStore

class SurnameSearcher(private val store: SurnameStore) {
    companion object {
        private const val TAG = "SurnameSearcher"
    }

    private val chosungStrategy = ChosungSearchStrategy(store)
    private val koreanStrategy = KoreanSearchStrategy(store)
    private val hanjaStrategy = HanjaSearchStrategy(store)
    private val infoBuilder = SurnameInfoBuilder(store)

    fun search(query: String): List<SurnameSearchResult> {
        if (!DataLoader.isReady()) {
            Log.e(TAG, "데이터가 아직 로드되지 않았습니다")
            return emptyList()
        }

        // store 상태 검증 추가
        if (store.surnameMapping.isEmpty() || store.charTripleDict.isEmpty()) {
            Log.e(TAG, "성씨 데이터가 비어있습니다")
            return emptyList()
        }

        val results = mutableListOf<SurnameSearchResult>()

        when {
            query.matches(Regex("[ㄱ-ㅎ]+")) -> chosungStrategy.search(query, results)
            query.matches(Regex("[가-힣]+")) -> koreanStrategy.search(query, results)
            else -> hanjaStrategy.search(query, results)
        }

        return results.distinctBy { "${it.korean}/${it.hanja}" }
            .sortedWith(compareBy({ !it.isCompound }, { it.korean }))
    }

    fun getSurnameInfo(korean: String, hanja: String): SurnameInfo? {
        return infoBuilder.build(korean, hanja)
    }
}