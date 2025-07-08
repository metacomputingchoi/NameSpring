// model/domain/service/search/hanja/matchers/WordMatcher.kt
package com.ssc.namespring.model.domain.service.search.hanja.matchers

import com.ssc.namespring.model.domain.service.search.hanja.scoring.MatchScoreType

class WordMatcher {
    fun getWordMatchScore(text: String, query: String): Float? {
        val queryWords = query.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val textWords = text.split(Regex("\\s+"))

        var matchCount = 0
        for (queryWord in queryWords) {
            if (textWords.any { it.contains(queryWord, ignoreCase = true) }) {
                matchCount++
            }
        }

        return if (matchCount > 0) {
            MatchScoreType.WORD_MATCH.baseScore * matchCount / queryWords.size
        } else {
            null
        }
    }
}