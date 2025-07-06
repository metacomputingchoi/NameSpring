// model/presentation/components/search/SearchResultMatcher.kt
package com.ssc.namespring.model.presentation.components.search

import com.ssc.namespring.model.common.utils.MixedPatternUtils

internal class SearchResultMatcher {

    fun checkSound(korean: String, query: String): Boolean {
        // 순수 초성 검색
        if (query.matches(Regex("^[ㄱ-ㅎ]+$"))) {
            return MixedPatternUtils.matchChosungPattern(korean, query)
        }

        // 혼합 패턴 검색
        if (MixedPatternUtils.containsMixedPattern(query)) {
            return MixedPatternUtils.matchMixedPattern(korean, query)
        }

        // 한글 검색
        if (query.matches(Regex("^[가-힣]+$"))) {
            if (korean == query) return true
            if (korean.contains(query)) return true
        }

        return false
    }

    fun checkMeaning(meaning: String?, query: String): Boolean {
        if (meaning == null || meaning.isEmpty()) return false

        // 순수 초성 검색
        if (query.matches(Regex("^[ㄱ-ㅎ]+$"))) {
            return MixedPatternUtils.matchChosungPattern(meaning, query)
        }

        // 혼합 패턴 검색
        if (MixedPatternUtils.containsMixedPattern(query)) {
            return MixedPatternUtils.matchMixedPattern(meaning, query)
        }

        // 일반 텍스트 검색
        return meaning.contains(query, ignoreCase = true)
    }

    fun checkHanja(hanja: String, query: String): Boolean {
        return hanja.contains(query)
    }

    fun checkStroke(strokes: Int, query: String): Boolean {
        return query.toIntOrNull() == strokes
    }
}