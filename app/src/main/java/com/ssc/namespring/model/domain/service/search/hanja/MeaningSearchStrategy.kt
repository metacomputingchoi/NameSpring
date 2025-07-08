// model/domain/service/search/hanja/MeaningSearchStrategy.kt
package com.ssc.namespring.model.domain.service.search.hanja

import android.util.Log
import com.ssc.namespring.model.data.mapper.OptimizedMapping
import com.ssc.namespring.model.data.mapper.HanjaInfo
import com.ssc.namespring.model.common.utils.MixedPatternUtils

internal class MeaningSearchStrategy(
    private val optimizedMapping: OptimizedMapping
) : HanjaSearchStrategy {

    companion object {
        private const val TAG = "MeaningSearchStrategy"
    }

    override fun search(query: String): List<HanjaInfo> {
        Log.d(TAG, "뜻 검색 모드: $query")

        val results = mutableListOf<HanjaInfo>()
        val scoreMap = mutableMapOf<String, Float>()

        // meaningSearchIndex에서 검색
        optimizedMapping.meaningSearchIndex.forEach { (word, hanjaList) ->
            val score = calculateMatchScore(word, query)
            if (score > 0) {
                hanjaList.forEach { hanja ->
                    val key = hanja.tripleKey
                    scoreMap[key] = maxOf(scoreMap[key] ?: 0f, score)
                    if (!results.any { it.tripleKey == key }) {
                        results.add(hanja)
                    }
                }
            }
        }

        // 직접 meaning 필드에서도 검색
        optimizedMapping.koreanToHanjaInfo.values.forEach { hanjaList ->
            hanjaList.forEach { info ->
                val meaning = info.meaning ?: ""
                if (meaning.isNotEmpty()) {
                    val score = calculateMatchScore(meaning, query)
                    if (score > 0 && results.none { it.tripleKey == info.tripleKey }) {
                        scoreMap[info.tripleKey] = score
                        results.add(info)
                    }
                }
            }
        }

        Log.d(TAG, "뜻 검색 결과: ${results.size}개")

        // 점수순으로 정렬
        return results
            .sortedByDescending { scoreMap[it.tripleKey] ?: 0f }
            .distinctBy { it.tripleKey }
    }

    private fun calculateMatchScore(text: String, query: String): Float {
        if (text.isEmpty() || query.isEmpty()) return 0f

        // 1. 완전 일치
        if (text.equals(query, ignoreCase = true)) return 10f

        // 2. 띄어쓰기 무시 완전 일치
        val textNoSpace = text.replace(" ", "")
        val queryNoSpace = query.replace(" ", "")
        if (textNoSpace.equals(queryNoSpace, ignoreCase = true)) return 9f

        // 3. 포함 검색 (띄어쓰기 포함)
        if (text.contains(query, ignoreCase = true)) return 8f

        // 4. 띄어쓰기 무시 포함 검색
        if (textNoSpace.contains(queryNoSpace, ignoreCase = true)) return 7f

        // 5. 초성 검색 (띄어쓰기 포함)
        if (query.matches(Regex("^[ㄱ-ㅎ\\s]+$"))) {
            if (matchesChosungWithSpace(text, query)) return 6f
        }

        // 6. 초성 검색 (띄어쓰기 무시)
        val queryNoSpaceChosung = queryNoSpace
        if (queryNoSpaceChosung.matches(Regex("^[ㄱ-ㅎ]+$"))) {
            if (MixedPatternUtils.matchChosungPattern(textNoSpace, queryNoSpaceChosung)) return 5f
        }

        // 7. 혼합 패턴 검색
        if (MixedPatternUtils.containsMixedPattern(query)) {
            if (MixedPatternUtils.matchMixedPattern(text, query)) return 4f
            if (MixedPatternUtils.matchMixedPattern(textNoSpace, queryNoSpace)) return 3f
        }

        // 8. 부분 단어 매칭
        val queryWords = query.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val textWords = text.split(Regex("\\s+"))

        var matchCount = 0
        for (queryWord in queryWords) {
            if (textWords.any { it.contains(queryWord, ignoreCase = true) }) {
                matchCount++
            }
        }

        if (matchCount > 0) {
            return 2f * matchCount / queryWords.size
        }

        // 9. 글자별 부분 매칭 (마지막 수단)
        var charMatchCount = 0
        for (char in queryNoSpace) {
            if (textNoSpace.contains(char, ignoreCase = true)) {
                charMatchCount++
            }
        }

        if (charMatchCount > 0 && charMatchCount >= queryNoSpace.length * 0.7) {
            return 1f * charMatchCount / queryNoSpace.length
        }

        return 0f
    }

    private fun matchesChosungWithSpace(text: String, chosungQuery: String): Boolean {
        // 텍스트를 초성으로 변환 (띄어쓰기 유지)
        val textChosung = text.map { char ->
            when {
                char == ' ' -> ' '
                char.toString().matches(Regex("[가-힣]")) -> {
                    HanjaSearchUtils.getChosung(char)
                }
                else -> ""
            }
        }.joinToString("")

        // 초성 쿼리와 비교 (띄어쓰기 포함)
        return textChosung.contains(chosungQuery)
    }
}