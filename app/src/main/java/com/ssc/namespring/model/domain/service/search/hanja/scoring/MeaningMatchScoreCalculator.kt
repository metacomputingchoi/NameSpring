// model/domain/service/search/hanja/scoring/MeaningMatchScoreCalculator.kt
package com.ssc.namespring.model.domain.service.search.hanja.scoring

import com.ssc.namespring.model.domain.service.search.hanja.matchers.*

class MeaningMatchScoreCalculator {
    private val textMatcher = TextMatcher()
    private val chosungMatcher = ChosungMatcher()
    private val mixedPatternMatcher = MixedPatternMatcher()
    private val wordMatcher = WordMatcher()

    fun calculateMatchScore(text: String, query: String): Float {
        if (text.isEmpty() || query.isEmpty()) return 0f

        // 완전 일치 검사
        textMatcher.getExactMatchScore(text, query)?.let { return it }

        // 포함 검사
        textMatcher.getContainsScore(text, query)?.let { return it }

        // 초성 검사
        chosungMatcher.getChosungScore(text, query)?.let { return it }

        // 혼합 패턴 검사
        mixedPatternMatcher.getMixedPatternScore(text, query)?.let { return it }

        // 단어 매칭
        wordMatcher.getWordMatchScore(text, query)?.let { return it }

        // 글자별 부분 매칭
        return getCharacterMatchScore(text, query)
    }

    private fun getCharacterMatchScore(text: String, query: String): Float {
        val textNoSpace = text.replace(" ", "")
        val queryNoSpace = query.replace(" ", "")

        var charMatchCount = 0
        for (char in queryNoSpace) {
            if (textNoSpace.contains(char, ignoreCase = true)) {
                charMatchCount++
            }
        }

        return if (charMatchCount > 0 && charMatchCount >= queryNoSpace.length * 0.7) {
            MatchScoreType.CHAR_MATCH.baseScore * charMatchCount / queryNoSpace.length
        } else {
            0f
        }
    }
}