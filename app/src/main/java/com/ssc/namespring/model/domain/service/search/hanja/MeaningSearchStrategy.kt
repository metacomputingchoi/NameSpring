// model/domain/service/search/hanja/MeaningSearchStrategy.kt
package com.ssc.namespring.model.domain.service.search.hanja

import android.util.Log
import com.ssc.namespring.model.data.mapper.OptimizedMapping
import com.ssc.namespring.model.data.mapper.HanjaInfo
import com.ssc.namespring.model.domain.service.search.hanja.scoring.MeaningMatchScoreCalculator

internal class MeaningSearchStrategy(
    private val optimizedMapping: OptimizedMapping
) : IHanjaSearchStrategy {

    companion object {
        private const val TAG = "MeaningSearchStrategy"
    }

    private val scoreCalculator = MeaningMatchScoreCalculator()

    override fun search(query: String): List<HanjaInfo> {
        Log.d(TAG, "뜻 검색 모드: $query")

        val searchResults = SearchResultCollector()

        // meaningSearchIndex에서 검색
        searchFromIndex(query, searchResults)

        // 직접 meaning 필드에서도 검색
        searchFromMeaningField(query, searchResults)

        Log.d(TAG, "뜻 검색 결과: ${searchResults.size}개")

        return searchResults.getSortedResults()
    }

    private fun searchFromIndex(query: String, collector: SearchResultCollector) {
        optimizedMapping.meaningSearchIndex.forEach { (word, hanjaList) ->
            val score = scoreCalculator.calculateMatchScore(word, query)
            if (score > 0) {
                hanjaList.forEach { hanja ->
                    collector.add(hanja, score)
                }
            }
        }
    }

    private fun searchFromMeaningField(query: String, collector: SearchResultCollector) {
        optimizedMapping.koreanToHanjaInfo.values.forEach { hanjaList ->
            hanjaList.forEach { info ->
                val meaning = info.meaning ?: ""
                if (meaning.isNotEmpty()) {
                    val score = scoreCalculator.calculateMatchScore(meaning, query)
                    if (score > 0) {
                        collector.add(info, score)
                    }
                }
            }
        }
    }

    private inner class SearchResultCollector {
        private val results = mutableListOf<HanjaInfo>()
        private val scoreMap = mutableMapOf<String, Float>()

        val size: Int get() = results.size

        fun add(hanja: HanjaInfo, score: Float) {
            val key = hanja.tripleKey
            val existingScore = scoreMap[key] ?: 0f

            if (existingScore == 0f) {
                results.add(hanja)
            }

            scoreMap[key] = maxOf(existingScore, score)
        }

        fun getSortedResults(): List<HanjaInfo> {
            return results
                .sortedByDescending { scoreMap[it.tripleKey] ?: 0f }
                .distinctBy { it.tripleKey }
        }
    }
}