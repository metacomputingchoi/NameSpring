// model/domain/service/search/hanja/HanjaQueryRouter.kt
package com.ssc.namespring.model.domain.service.search.hanja

import android.util.Log
import com.ssc.namespring.model.data.mapper.OptimizedMapping
import com.ssc.namespring.model.data.mapper.HanjaInfo

internal class HanjaQueryRouter(private val optimizedMapping: OptimizedMapping) {
    companion object {
        private const val TAG = "HanjaQueryRouter"
    }

    private val chosungStrategy = ChosungSearchMapStrategy(optimizedMapping)
    private val koreanStrategy = KoreanSearchMapStrategy(optimizedMapping)
    private val hanjaCharStrategy = HanjaCharSearchStrategy(optimizedMapping)
    private val meaningStrategy = MeaningSearchStrategy(optimizedMapping)

    fun route(query: String): List<HanjaInfo> {
        return when {
            // 초성 검색 (ㄱ, ㄱㅅ 등)
            isChosungQuery(query) -> {
                Log.d(TAG, "초성 검색으로 라우팅: $query")
                chosungStrategy.search(query)
            }

            // 한글 검색 (단일 글자 또는 여러 글자)
            isKoreanQuery(query) -> {
                Log.d(TAG, "한글 검색으로 라우팅: $query")
                koreanStrategy.search(query)
            }

            // 한자 검색 (한자가 포함된 경우)
            containsHanja(query) -> {
                Log.d(TAG, "한자 검색으로 라우팅: $query")
                hanjaCharStrategy.search(query)
            }

            // 그 외는 모두 뜻 검색 (숫자, 영어, 한글 조합 등)
            else -> {
                Log.d(TAG, "뜻 검색으로 라우팅: $query")
                meaningStrategy.search(query)
            }
        }
    }

    private fun isChosungQuery(query: String): Boolean {
        return query.matches(Regex("^[ㄱ-ㅎ]+$"))
    }

    private fun isKoreanQuery(query: String): Boolean {
        return query.matches(Regex("^[가-힣]+$"))
    }

    private fun containsHanja(text: String): Boolean {
        return HanjaSearchUtils.containsHanja(text)
    }
}