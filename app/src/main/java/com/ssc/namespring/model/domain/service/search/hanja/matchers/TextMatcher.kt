// model/domain/service/search/hanja/matchers/TextMatcher.kt
package com.ssc.namespring.model.domain.service.search.hanja.matchers

import com.ssc.namespring.model.domain.service.search.hanja.scoring.MatchScoreType

class TextMatcher {
    fun getExactMatchScore(text: String, query: String): Float? {
        // 완전 일치
        if (text.equals(query, ignoreCase = true)) {
            return MatchScoreType.EXACT_MATCH.baseScore
        }

        // 띄어쓰기 무시 완전 일치
        val textNoSpace = text.replace(" ", "")
        val queryNoSpace = query.replace(" ", "")
        if (textNoSpace.equals(queryNoSpace, ignoreCase = true)) {
            return MatchScoreType.EXACT_MATCH_NO_SPACE.baseScore
        }

        return null
    }

    fun getContainsScore(text: String, query: String): Float? {
        // 포함 검색 (띄어쓰기 포함)
        if (text.contains(query, ignoreCase = true)) {
            return MatchScoreType.CONTAINS.baseScore
        }

        // 띄어쓰기 무시 포함 검색
        val textNoSpace = text.replace(" ", "")
        val queryNoSpace = query.replace(" ", "")
        if (textNoSpace.contains(queryNoSpace, ignoreCase = true)) {
            return MatchScoreType.CONTAINS_NO_SPACE.baseScore
        }

        return null
    }
}